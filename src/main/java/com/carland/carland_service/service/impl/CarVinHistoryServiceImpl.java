package com.carland.carland_service.service.impl;

import com.carland.carland_service.dto.response.CarVinServiceHistoryResponse;
import com.carland.carland_service.dto.response.ServiceHistoryItemResponse;
import com.carland.carland_service.dto.response.ServiceHistoryPartResponse;
import com.carland.carland_service.dto.response.hyper.HyperServiceHistoryItemResponse;
import com.carland.carland_service.dto.response.hyper.HyperServicePartResponse;
import com.carland.carland_service.dto.response.hyper.HyperVehicleByVinResponse;
import com.carland.carland_service.entity.Car;
import com.carland.carland_service.entity.Customer;
import com.carland.carland_service.entity.ServiceHistory;
import com.carland.carland_service.entity.ServiceHistoryPart;
import com.carland.carland_service.enums.EnumMessagesLangValues;
import com.carland.carland_service.enums.EnumUserStatus;
import com.carland.carland_service.enums.ServiceTypeTranslation;
import com.carland.carland_service.exceptions.MissingFieldException;
import com.carland.carland_service.exceptions.ResourceNotFoundException;
import com.carland.carland_service.exceptions.UserNotFoundException;
import com.carland.carland_service.repository.CarRepository;
import com.carland.carland_service.repository.CustomerRepository;
import com.carland.carland_service.repository.ServiceHistoryPartRepository;
import com.carland.carland_service.repository.ServiceHistoryRepository;
import com.carland.carland_service.service.interfaces.CarVinHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarVinHistoryServiceImpl implements CarVinHistoryService {

    private static final String HYPERSERVICE_SOURCE = "hyperservice";
    private static final String CACHE_SOURCE = "cache";

    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;
    private final ServiceHistoryRepository serviceHistoryRepository;
    private final ServiceHistoryPartRepository serviceHistoryPartRepository;
    private final HyperTokenService hyperTokenService;
    private final RestTemplate restTemplate;

    @Value("${hyper.auth.base-url}")
    private String hyperBaseUrl;

    @Override
    @Transactional
    public CarVinServiceHistoryResponse getServiceHistoryByVin(String vin, String phoneNumber, String userIdHeader, String acceptLanguage) {
        if (vin == null || vin.isBlank() || phoneNumber == null || userIdHeader == null) {
            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
        }

        Customer customer = customerRepository.findByUserIdAndPhoneNumberAndStatus(Long.valueOf(userIdHeader), phoneNumber, EnumUserStatus.ACTIVE.name());
        if (customer == null) {
            throw new UserNotFoundException(EnumMessagesLangValues.USER_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        Car car = carRepository.findByVinAndCustomer(vin, customer);
        if (car == null) {
            throw new ResourceNotFoundException(EnumMessagesLangValues.CAR_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        List<ServiceHistory> existingRows = serviceHistoryRepository.findAllByCarOrderByDoneDateDescIdDesc(car);
        if (!existingRows.isEmpty()) {
            return CarVinServiceHistoryResponse.builder()
                    .vin(vin)
                    .source(CACHE_SOURCE)
                    .items(existingRows.stream().map(row -> mapServiceHistory(row, acceptLanguage)).toList())
                    .build();
        }

        HyperVehicleByVinResponse hyperResponse = fetchHyperHistory(vin);
        if (hyperResponse == null || hyperResponse.getServiceHistory() == null || hyperResponse.getServiceHistory().isEmpty()) {
            return CarVinServiceHistoryResponse.builder()
                    .vin(vin)
                    .source(HYPERSERVICE_SOURCE)
                    .items(Collections.emptyList())
                    .build();
        }

        List<ServiceHistory> persisted = persistHyperRows(car, hyperResponse.getServiceHistory());
        return CarVinServiceHistoryResponse.builder()
                .vin(vin)
                .source(HYPERSERVICE_SOURCE)
                .items(persisted.stream().map(row -> mapServiceHistory(row, acceptLanguage)).toList())
                .build();
    }

    private HyperVehicleByVinResponse fetchHyperHistory(String vin) {
        String token = hyperTokenService.getToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = hyperBaseUrl + "/partner/v1/vehicles/by-vin/" + vin;
        try {
            ResponseEntity<HyperVehicleByVinResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, HyperVehicleByVinResponse.class
            );
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            if (e.getResponseBodyAsString() != null && e.getResponseBodyAsString().contains("vehicle_not_found")) {
                return null;
            }
            throw e;
        }
    }

    private List<ServiceHistory> persistHyperRows(Car car, List<HyperServiceHistoryItemResponse> items) {
        List<ServiceHistory> persisted = new ArrayList<>();
        for (HyperServiceHistoryItemResponse item : items) {
            List<String> groups = item.getServiceGroups() == null ? Collections.emptyList() : item.getServiceGroups();

            ServiceHistory history = ServiceHistory.builder()
                    .car(car)
                    .serviceName(item.getServiceType())
                    .actionType(groups)
                    .doneDate(item.getLastServiceDate())
                    .doneKm(item.getLastServiceMileage())
                    .serviceCenter(null)
                    .serviceAmount(item.getFinalCost() != null ? item.getFinalCost().getAmount() : null)
                    .dealer(item.getDealer())
                    .nextServiceDate(item.getNextServiceDate())
                    .nextServiceMileage(item.getNextServiceMileage())
                    .source(HYPERSERVICE_SOURCE)
                    .build();

            ServiceHistory saved = serviceHistoryRepository.save(history);
            persistParts(saved, item.getParts());
            persisted.add(saved);
        }

        return persisted.stream()
                .sorted(Comparator.comparing(ServiceHistory::getDoneDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ServiceHistory::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    private void persistParts(ServiceHistory serviceHistory, List<HyperServicePartResponse> parts) {
        if (parts == null || parts.isEmpty()) {
            return;
        }

        List<ServiceHistoryPart> entities = parts.stream()
                .map(part -> ServiceHistoryPart.builder()
                        .serviceHistory(serviceHistory)
                        .name(part.getName())
                        .qty(part.getQty())
                        .unit(part.getUnit())
                        .cost(null)
                        .finalCost(null)
                        .discount(null)
                        .build())
                .toList();

        serviceHistoryPartRepository.saveAll(entities);
    }

    private ServiceHistoryItemResponse mapServiceHistory(ServiceHistory history, String acceptLanguage) {
        List<ServiceHistoryPartResponse> parts = history.getId() == null
                ? Collections.emptyList()
                : serviceHistoryPartRepository.findAllByServiceHistory(history).stream()
                .map(part -> ServiceHistoryPartResponse.builder()
                        .name(part.getName())
                        .qty(part.getQty())
                        .unit(part.getUnit())
                        .cost(part.getCost())
                        .finalCost(part.getFinalCost())
                        .discount(part.getDiscount())
                        .build())
                .toList();

        List<String> actionType = history.getActionType() == null
                ? Collections.emptyList()
                : ServiceTypeTranslation.translateList(history.getActionType(), acceptLanguage);

        return ServiceHistoryItemResponse.builder()
                .id(history.getId())
                .serviceName(ServiceTypeTranslation.translate(history.getServiceName(), acceptLanguage))
                .actionType(actionType)
                .doneDate(history.getDoneDate())
                .doneKM(history.getDoneKm())
                .serviceCenter(history.getServiceCenter())
                .serviceCenterId(history.getServiceCenterId() != null ? history.getServiceCenterId() : 1L)
                .serviceAmount(history.getServiceAmount())
                .dealer(history.getDealer())
                .parts(parts)
                .nextServiceDate(history.getNextServiceDate())
                .nextServiceMileage(history.getNextServiceMileage())
                .build();
    }
}

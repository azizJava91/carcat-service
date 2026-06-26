package com.carland.carland_service.service.impl;

import com.carland.carland_service.dto.response.MoneyResponse;
import com.carland.carland_service.dto.response.hyper.HyperCostResponse;
import com.carland.carland_service.dto.response.hyper.HyperServiceHistoryItemResponse;
import com.carland.carland_service.dto.response.hyper.HyperServiceLineResponse;
import com.carland.carland_service.dto.response.hyper.HyperServicePartResponse;
import com.carland.carland_service.dto.response.hyper.HyperVehicleByVinResponse;
import com.carland.carland_service.dto.response.v2.CarVinServiceHistoryV2Response;
import com.carland.carland_service.dto.response.v2.ServiceHistoryLineV2Response;
import com.carland.carland_service.dto.response.v2.ServiceHistoryPartV2Response;
import com.carland.carland_service.dto.response.v2.ServiceHistorySummaryV2Response;
import com.carland.carland_service.dto.response.v2.ServiceHistoryVisitV2Response;
import com.carland.carland_service.entity.Car;
import com.carland.carland_service.entity.Customer;
import com.carland.carland_service.entity.ServiceEntity;
import com.carland.carland_service.entity.ServiceHistory;
import com.carland.carland_service.entity.ServiceHistoryLine;
import com.carland.carland_service.entity.ServiceHistoryPart;
import com.carland.carland_service.enums.EnumMessagesLangValues;
import com.carland.carland_service.enums.EnumUserStatus;
import com.carland.carland_service.exceptions.MissingFieldException;
import com.carland.carland_service.exceptions.ResourceNotFoundException;
import com.carland.carland_service.exceptions.UserNotFoundException;
import com.carland.carland_service.repository.CarRepository;
import com.carland.carland_service.repository.CustomerRepository;
import com.carland.carland_service.repository.ServiceEntityRepository;
import com.carland.carland_service.repository.ServiceHistoryLineRepository;
import com.carland.carland_service.repository.ServiceHistoryPartRepository;
import com.carland.carland_service.repository.ServiceHistoryRepository;
import com.carland.carland_service.service.interfaces.CarVinHistoryV2Service;
import com.carland.carland_service.util.HyperPartnerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarVinHistoryV2ServiceImpl implements CarVinHistoryV2Service {

    private static final String HYPERSERVICE_SOURCE = "hyperservice";
    private static final String CACHE_SOURCE = "cache";
    private static final String LIVE_SOURCE = "live";
    private static final String DEFAULT_CURRENCY = "AZN";

    private static final ConcurrentHashMap<Long, Object> CAR_PERSIST_LOCKS = new ConcurrentHashMap<>();

    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;
    private final ServiceHistoryRepository serviceHistoryRepository;
    private final ServiceHistoryLineRepository serviceHistoryLineRepository;
    private final ServiceHistoryPartRepository serviceHistoryPartRepository;
    private final ServiceEntityRepository serviceEntityRepository;
    private final HyperTokenService hyperTokenService;
    private final RestTemplate restTemplate;

    @Value("${hyper.auth.base-url}")
    private String hyperBaseUrl;

    @Override
    @Transactional
    public CarVinServiceHistoryV2Response getServiceHistoryByVin(String vin, String phoneNumber, String userIdHeader, String acceptLanguage) {
        if (vin == null || vin.isBlank() || phoneNumber == null || userIdHeader == null) {
            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
        }

        Customer customer = customerRepository.findByUserIdAndPhoneNumberAndStatus(
                Long.valueOf(userIdHeader), phoneNumber, EnumUserStatus.ACTIVE.name());
        if (customer == null) {
            throw new UserNotFoundException(EnumMessagesLangValues.USER_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        Car car = carRepository.findByVinAndCustomer(vin, customer);
        if (car == null) {
            throw new ResourceNotFoundException(EnumMessagesLangValues.CAR_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        List<ServiceHistory> cachedRows = serviceHistoryRepository.findAllByCarAndPartnerRecordIdIsNotNullOrderByDoneDateDescIdDesc(car);
        if (!cachedRows.isEmpty()) {
            return buildResponse(vin, CACHE_SOURCE, cachedRows);
        }

        synchronized (lockForCar(car.getCarId())) {
            cachedRows = serviceHistoryRepository.findAllByCarAndPartnerRecordIdIsNotNullOrderByDoneDateDescIdDesc(car);
            if (!cachedRows.isEmpty()) {
                return buildResponse(vin, CACHE_SOURCE, cachedRows);
            }

            HyperVehicleByVinResponse hyperResponse = fetchHyperHistory(vin);
            if (hyperResponse == null || hyperResponse.getServiceHistory() == null || hyperResponse.getServiceHistory().isEmpty()) {
                return buildResponse(vin, LIVE_SOURCE, Collections.emptyList());
            }

            List<ServiceHistory> persisted = persistHyperRows(car, hyperResponse.getServiceHistory());
            return buildResponse(vin, LIVE_SOURCE, persisted);
        }
    }

    private Object lockForCar(Long carId) {
        return CAR_PERSIST_LOCKS.computeIfAbsent(carId, id -> new Object());
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
            if (item.getRecordId() == null) {
                log.warn("Skipping Hyper visit without recordId for carId={}", car.getCarId());
                continue;
            }

            ServiceHistory visit = serviceHistoryRepository.findByCarAndPartnerRecordId(car, item.getRecordId())
                    .orElse(null);

            if (visit == null) {
                visit = ServiceHistory.builder()
                        .car(car)
                        .partnerRecordId(item.getRecordId())
                        .lines(new ArrayList<>())
                        .build();
            } else {
                serviceHistoryLineRepository.deleteByServiceHistory(visit);
                serviceHistoryPartRepository.deleteByServiceHistory(visit);
                visit.getLines().clear();
            }

            List<String> groups = item.getServiceGroups() == null ? Collections.emptyList() : item.getServiceGroups();
            HyperCostResponse finalCost = item.getFinalCost();
            Long serviceCenterId = HyperPartnerRegistry.resolveServiceCenterId(null);

            visit.setServiceName(item.getServiceType());
            visit.setActionType(groups);
            visit.setDoneDate(item.getLastServiceDate());
            visit.setDoneKm(item.getLastServiceMileage());
            visit.setServiceCenterId(serviceCenterId);
            visit.setServiceCenter(HyperPartnerRegistry.resolveServiceCenterName(serviceCenterId));
            visit.setServiceAmount(finalCost != null ? finalCost.getAmount() : null);
            visit.setAmountCurrency(finalCost != null && finalCost.getCurrency() != null ? finalCost.getCurrency() : DEFAULT_CURRENCY);
            visit.setDealer(item.getDealer());
            visit.setNextServiceDate(null);
            visit.setNextServiceMileage(null);
            visit.setSource(HYPERSERVICE_SOURCE);

            ServiceHistory savedVisit = serviceHistoryRepository.save(visit);
            persistLines(savedVisit, item.getServices(), car);
            persistParts(savedVisit, item.getParts());
            persisted.add(savedVisit);
        }

        return persisted.stream()
                .sorted(Comparator.comparing(ServiceHistory::getDoneDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ServiceHistory::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    private void persistLines(ServiceHistory visit, List<HyperServiceLineResponse> hyperLines, Car car) {
        if (hyperLines == null || hyperLines.isEmpty()) {
            return;
        }

        List<ServiceHistoryLine> lines = new ArrayList<>();
        for (HyperServiceLineResponse hyperLine : hyperLines) {
            ServiceHistoryLine line = ServiceHistoryLine.builder()
                    .serviceHistory(visit)
                    .serviceCode(hyperLine.getServiceCode())
                    .universalServiceId(hyperLine.getUniversalServiceId())
                    .serviceName(hyperLine.getServiceName())
                    .costAmount(hyperLine.getCost() != null ? hyperLine.getCost().getAmount() : null)
                    .costCurrency(hyperLine.getCost() != null && hyperLine.getCost().getCurrency() != null
                            ? hyperLine.getCost().getCurrency() : DEFAULT_CURRENCY)
                    .build();

            computeAndSetNextService(line, visit, car);
            lines.add(line);
        }

        serviceHistoryLineRepository.saveAll(lines);
        visit.getLines().addAll(lines);
    }

    private void computeAndSetNextService(ServiceHistoryLine line, ServiceHistory visit, Car car) {
        if (line.getUniversalServiceId() == null) {
            return;
        }

        Optional<ServiceEntity> serviceEntityOpt = serviceEntityRepository.findById(line.getUniversalServiceId());
        if (serviceEntityOpt.isEmpty()) {
            return;
        }

        ServiceEntity serviceEntity = serviceEntityOpt.get();
        if (car.getMaintenanceTemplate() != null
                && serviceEntity.getMaintenanceTemplate() != null
                && !car.getMaintenanceTemplate().getId().equals(serviceEntity.getMaintenanceTemplate().getId())) {
            return;
        }

        if (serviceEntity.getIntervalMonth() != null && visit.getDoneDate() != null) {
            line.setNextServiceDate(visit.getDoneDate().plusMonths(serviceEntity.getIntervalMonth()));
        }
        if (serviceEntity.getIntervalKm() != null && visit.getDoneKm() != null) {
            line.setNextServiceMileage(visit.getDoneKm() + serviceEntity.getIntervalKm().intValue());
        }
    }

    private void persistParts(ServiceHistory visit, List<HyperServicePartResponse> parts) {
        if (parts == null || parts.isEmpty()) {
            return;
        }

        List<ServiceHistoryPart> entities = parts.stream()
                .map(part -> ServiceHistoryPart.builder()
                        .serviceHistory(visit)
                        .name(part.getName())
                        .qty(part.getQty())
                        .unit(part.getUnit())
                        .build())
                .toList();

        serviceHistoryPartRepository.saveAll(entities);
    }

    private CarVinServiceHistoryV2Response buildResponse(String vin, String source, List<ServiceHistory> visits) {
        List<ServiceHistoryVisitV2Response> items = visits.stream()
                .map(this::mapVisit)
                .toList();

        BigDecimal total = items.stream()
                .map(item -> item.getAmount() != null ? item.getAmount().getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CarVinServiceHistoryV2Response.builder()
                .vin(vin)
                .source(source)
                .summary(ServiceHistorySummaryV2Response.builder()
                        .serviceCount(items.size())
                        .totalAmount(MoneyResponse.builder()
                                .amount(total)
                                .currency(DEFAULT_CURRENCY)
                                .build())
                        .build())
                .items(items)
                .build();
    }

    private ServiceHistoryVisitV2Response mapVisit(ServiceHistory visit) {
        List<ServiceHistoryLineV2Response> services = visit.getId() == null
                ? Collections.emptyList()
                : serviceHistoryLineRepository.findAllByServiceHistory(visit).stream()
                .map(this::mapLine)
                .toList();

        List<ServiceHistoryPartV2Response> parts = visit.getId() == null
                ? Collections.emptyList()
                : serviceHistoryPartRepository.findAllByServiceHistory(visit).stream()
                .map(part -> ServiceHistoryPartV2Response.builder()
                        .name(part.getName())
                        .qty(part.getQty())
                        .unit(part.getUnit())
                        .build())
                .toList();

        Long serviceCenterId = visit.getServiceCenterId() != null
                ? visit.getServiceCenterId()
                : HyperPartnerRegistry.HYPERSERVICE_CENTER_ID;

        return ServiceHistoryVisitV2Response.builder()
                .id(visit.getId())
                .partnerRecordId(visit.getPartnerRecordId())
                .type(visit.getServiceName())
                .serviceGroups(visit.getActionType() == null ? Collections.emptyList() : visit.getActionType())
                .services(services)
                .date(visit.getDoneDate())
                .mileage(visit.getDoneKm())
                .serviceCenterId(serviceCenterId)
                .serviceCenterName(visit.getServiceCenter() != null
                        ? visit.getServiceCenter()
                        : HyperPartnerRegistry.resolveServiceCenterName(serviceCenterId))
                .dealer(visit.getDealer())
                .amount(toMoney(visit.getServiceAmount(), visit.getAmountCurrency()))
                .parts(parts)
                .build();
    }

    private ServiceHistoryLineV2Response mapLine(ServiceHistoryLine line) {
        return ServiceHistoryLineV2Response.builder()
                .serviceCode(line.getServiceCode())
                .universalServiceId(line.getUniversalServiceId())
                .serviceName(line.getServiceName())
                .cost(toMoney(line.getCostAmount(), line.getCostCurrency()))
                .nextServiceDate(line.getNextServiceDate())
                .nextServiceMileage(line.getNextServiceMileage())
                .build();
    }

    private MoneyResponse toMoney(BigDecimal amount, String currency) {
        if (amount == null) {
            return null;
        }
        return MoneyResponse.builder()
                .amount(amount)
                .currency(currency != null ? currency : DEFAULT_CURRENCY)
                .build();
    }
}

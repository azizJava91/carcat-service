package com.carland.carland_service.dto.response.v2;

import com.carland.carland_service.dto.response.hyper.HyperCostResponse;
import com.carland.carland_service.dto.response.hyper.HyperServiceHistoryItemResponse;
import com.carland.carland_service.dto.response.hyper.HyperServiceLineResponse;
import com.carland.carland_service.dto.response.hyper.HyperServicePartResponse;
import com.carland.carland_service.dto.response.hyper.HyperVehicleByVinMockData;
import com.carland.carland_service.dto.response.hyper.HyperVehicleByVinResponse;
import com.carland.carland_service.entity.Car;
import com.carland.carland_service.entity.Customer;
import com.carland.carland_service.entity.Partner;
import com.carland.carland_service.enums.EnumMessagesLangValues;
import com.carland.carland_service.enums.EnumPartnerId;
import com.carland.carland_service.enums.EnumUserStatus;
import com.carland.carland_service.enums.ServiceTypeTranslation;
import com.carland.carland_service.exceptions.MissingFieldException;
import com.carland.carland_service.exceptions.ResourceNotFoundException;
import com.carland.carland_service.exceptions.UserNotFoundException;
import com.carland.carland_service.repository.CarRepository;
import com.carland.carland_service.repository.CustomerRepository;
import com.carland.carland_service.repository.VisitRepository;
import com.carland.carland_service.service.HyperPercentageSyncService;
import com.carland.carland_service.service.PartnerLookupService;
import com.carland.carland_service.service.impl.HyperTokenService;
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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarVinHistoryServiceV2Impl implements CarVinHistoryServiceV2 {

    private static final String CACHE_SOURCE = "cache";
    private static final String LIVE_SOURCE = "live";
    private static final EnumPartnerId HYPER_PARTNER = EnumPartnerId.HYPER;

    private static final ConcurrentHashMap<Long, Object> CAR_PERSIST_LOCKS = new ConcurrentHashMap<>();

    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;
    private final VisitRepository visitRepository;
    private final HyperPercentageSyncService hyperPercentageSyncService;
    private final PartnerLookupService partnerLookupService;
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

        List<Visit> cachedVisits = visitRepository.findAllByCarOrderByLastServiceDateDescIdDesc(car);
        if (!cachedVisits.isEmpty()) {
            hyperPercentageSyncService.syncFromVisits(car, cachedVisits);
            return buildResponse(car, vin, CACHE_SOURCE, cachedVisits, acceptLanguage);
        }

        synchronized (lockForCar(car.getCarId())) {
            cachedVisits = visitRepository.findAllByCarOrderByLastServiceDateDescIdDesc(car);
            if (!cachedVisits.isEmpty()) {
                hyperPercentageSyncService.syncFromVisits(car, cachedVisits);
                return buildResponse(car, vin, CACHE_SOURCE, cachedVisits, acceptLanguage);
            }

            HyperVehicleByVinResponse hyperResponse = fetchHyperHistory(vin);
            if (hyperResponse == null || hyperResponse.getServiceHistory() == null || hyperResponse.getServiceHistory().isEmpty()) {
                return buildResponse(car, vin, LIVE_SOURCE, Collections.emptyList(), acceptLanguage);
            }

            List<Visit> persisted = persistHyperVisits(car, hyperResponse.getServiceHistory());
            hyperPercentageSyncService.syncFromVisits(car, persisted);
            return buildResponse(car, vin, LIVE_SOURCE, persisted, acceptLanguage);
        }
    }

    private CarVinServiceHistoryV2Response buildResponse(Car car, String vin, String source, List<Visit> visits, String acceptLanguage) {
        Map<Long, Partner> partnerById = loadPartnersForVisits(visits);
        List<ServiceHistoryVisitV2Response> items = visits.stream()
                .map(visit -> mapVisit(visit, partnerById, acceptLanguage))
                .toList();

        return CarVinServiceHistoryV2Response.builder()
                .vin(vin)
                .source(source)
                .summary(buildSummary(items))
                .items(items)
                .allTimeCost(car.getAllTimeCost())
                .build();
    }

    private ServiceHistorySummaryV2Response buildSummary(List<ServiceHistoryVisitV2Response> items) {
        BigDecimal total = items.stream()
                .map(item -> item.getAmount() != null ? item.getAmount().getAmount() : null)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String currency = items.stream()
                .map(item -> item.getAmount() != null ? item.getAmount().getCurrency() : null)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("AZN");

        return ServiceHistorySummaryV2Response.builder()
                .serviceCount(items.size())
                .totalAmount(MoneyResponse.builder().amount(total).currency(currency).build())
                .build();
    }

    private Map<Long, Partner> loadPartnersForVisits(List<Visit> visits) {
        Set<Long> partnerIds = visits.stream()
                .map(Visit::getServiceCenterId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
        partnerIds.add(HYPER_PARTNER.getId());
        return partnerLookupService.loadByIds(partnerIds);
    }

    private ServiceHistoryVisitV2Response mapVisit(Visit visit, Map<Long, Partner> partnerById, String acceptLanguage) {
        Long partnerId = partnerLookupService.resolvePartnerId(visit.getServiceCenterId(), HYPER_PARTNER);
        EnumPartnerId enumPartner = EnumPartnerId.fromId(partnerId).orElse(HYPER_PARTNER);
        String partnerName = partnerLookupService.resolvePartnerName(
                visit.getServiceCenterId(), visit.getServiceCenterName(), partnerById, enumPartner);

        List<String> serviceGroups = visit.getServiceGroups() == null
                ? Collections.emptyList()
                : ServiceTypeTranslation.translateList(visit.getServiceGroups(), acceptLanguage);

        return ServiceHistoryVisitV2Response.builder()
                .id(visit.getId())
                .partnerRecordId(visit.getHyperRecordId())
                .type(ServiceTypeTranslation.translate(visit.getServiceType(), acceptLanguage))
                .serviceGroups(serviceGroups)
                .services(mapServiceLines(visit.getServices()))
                .date(visit.getLastServiceDate())
                .mileage(visit.getLastServiceMileage())
                .serviceCenterId(partnerId)
                .serviceCenterName(partnerName)
                .partner(partnerLookupService.toDataResponse(partnerById.get(partnerId), enumPartner))
                .dealer(visit.getDealer())
                .amount(toMoney(visit.getFinalCostAmount(), visit.getFinalCostCurrency()))
                .parts(mapParts(visit.getParts()))
                .build();
    }

    private List<ServiceHistoryLineV2Response> mapServiceLines(List<ServiceHistoryV2> lines) {
        if (lines == null || lines.isEmpty()) {
            return Collections.emptyList();
        }
        return lines.stream()
                .map(line -> ServiceHistoryLineV2Response.builder()
                        .serviceCode(line.getServiceCode())
                        .universalServiceId(line.getUniversalServiceId())
                        .serviceName(line.getServiceName())
                        .cost(toMoney(line.getCostAmount(), line.getCostCurrency()))
                        .nextServiceDate(line.getNextServiceDate())
                        .nextServiceMileage(line.getNextServiceMileage())
                        .build())
                .toList();
    }

    private List<ServiceHistoryPartV2Response> mapParts(List<ServiceHistoryPartV2> parts) {
        if (parts == null || parts.isEmpty()) {
            return Collections.emptyList();
        }
        return parts.stream()
                .map(part -> ServiceHistoryPartV2Response.builder()
                        .name(part.getName())
                        .qty(part.getQty())
                        .unit(part.getUnit())
                        .build())
                .toList();
    }

    private MoneyResponse toMoney(BigDecimal amount, String currency) {
        if (amount == null && currency == null) {
            return null;
        }
        return MoneyResponse.builder()
                .amount(amount)
                .currency(currency != null ? currency : "AZN")
                .build();
    }

    private Object lockForCar(Long carId) {
        return CAR_PERSIST_LOCKS.computeIfAbsent(carId, id -> new Object());
    }

    private HyperVehicleByVinResponse fetchHyperHistory(String vin) {
        return HyperVehicleByVinMockData.load(vin);

        /*
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
        */
    }

    private List<Visit> persistHyperVisits(Car car, List<HyperServiceHistoryItemResponse> items) {
        List<Visit> persisted = new ArrayList<>();
        Set<Long> seenRecordIds = new HashSet<>();
        BigDecimal allTimeCost = BigDecimal.ZERO;

        for (HyperServiceHistoryItemResponse item : items) {
            if (item.getRecordId() == null) {
                log.warn("Skipping Hyper visit without recordId for carId={}", car.getCarId());
                continue;
            }
            if (!seenRecordIds.add(item.getRecordId())) {
                log.info("Skipping duplicate Hyper recordId in same response for carId={}: {}", car.getCarId(), item.getRecordId());
                continue;
            }

            allTimeCost = allTimeCost.add(resolveVisitFinalCost(item));

            Optional<Visit> existing = visitRepository.findByCarAndHyperRecordId(car, item.getRecordId());
            if (existing.isPresent()) {
                log.info("Skipping duplicate Hyper visit for carId={}, recordId={}", car.getCarId(), item.getRecordId());
                persisted.add(existing.get());
                continue;
            }

            Visit visit = mapHyperItemToVisit(car, item);
            Visit saved = visitRepository.save(visit);
            persisted.add(saved);
        }

        car.setAllTimeCost(allTimeCost);
        carRepository.save(car);
        log.info("Updated allTimeCost for carId={} | total={}", car.getCarId(), allTimeCost);

        return persisted.stream()
                .sorted(Comparator.comparing(Visit::getLastServiceDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Visit::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    /** Visit total from Hyper: finalCost (post-discount) is authoritative; cost is fallback only. */
    private BigDecimal resolveVisitFinalCost(HyperServiceHistoryItemResponse item) {
        if (item.getFinalCost() != null && item.getFinalCost().getAmount() != null) {
            return item.getFinalCost().getAmount();
        }
        if (item.getCost() != null && item.getCost().getAmount() != null) {
            return item.getCost().getAmount();
        }
        return BigDecimal.ZERO;
    }

    private Visit mapHyperItemToVisit(Car car, HyperServiceHistoryItemResponse item) {
        Partner partner = partnerLookupService.find(HYPER_PARTNER).orElse(null);
        Long partnerId = HYPER_PARTNER.getId();
        String partnerName = partner != null ? partner.getName() : HYPER_PARTNER.getDefaultName();

        Visit visit = Visit.builder()
                .car(car)
                .hyperRecordId(item.getRecordId())
                .serviceType(item.getServiceType())
                .lastServiceDate(item.getLastServiceDate())
                .lastServiceMileage(item.getLastServiceMileage())
                .invoiceNumber(item.getInvoiceNumber())
                .dealer(item.getDealer())
                .serviceCenterId(partnerId)
                .serviceCenterName(partnerName)
                .costAmount(item.getCost() != null ? item.getCost().getAmount() : null)
                .costCurrency(item.getCost() != null ? item.getCost().getCurrency() : null)
                .finalCostAmount(item.getFinalCost() != null ? item.getFinalCost().getAmount() : null)
                .finalCostCurrency(item.getFinalCost() != null ? item.getFinalCost().getCurrency() : null)
                .serviceGroups(item.getServiceGroups() != null ? new ArrayList<>(item.getServiceGroups()) : new ArrayList<>())
                .build();

        if (item.getServices() != null) {
            for (HyperServiceLineResponse line : item.getServices()) {
                visit.addService(mapHyperLineToEntity(line));
            }
        }
        if (item.getParts() != null) {
            for (HyperServicePartResponse part : item.getParts()) {
                visit.addPart(ServiceHistoryPartV2.builder()
                        .name(part.getName())
                        .qty(part.getQty())
                        .unit(part.getUnit())
                        .build());
            }
        }

        return visit;
    }

    private ServiceHistoryV2 mapHyperLineToEntity(HyperServiceLineResponse line) {
        return ServiceHistoryV2.builder()
                .serviceCode(line.getServiceCode())
                .serviceName(line.getServiceName())
                .universalServiceId(parseUniversalServiceId(line.getUniversalServiceId()))
                .serviceGroups(line.getServiceGroups() != null ? new ArrayList<>(line.getServiceGroups()) : new ArrayList<>())
                .costAmount(line.getCost() != null ? line.getCost().getAmount() : null)
                .costCurrency(line.getCost() != null ? line.getCost().getCurrency() : null)
                .nextServiceDate(line.getNextServiceDate())
                .nextServiceMileage(line.getNextServiceMileage())
                .build();
    }

    private Long parseUniversalServiceId(String raw) {
        if (raw == null || raw.isBlank() || "other".equalsIgnoreCase(raw)) {
            return null;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            log.debug("Non-numeric universalServiceId from Hyper, skipping: {}", raw);
            return null;
        }
    }
}

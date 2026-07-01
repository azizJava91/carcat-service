package com.carland.carland_service.service.impl;

import com.carland.carland_service.dto.request.PartnerUpdateServiceVisitRequest;
import com.carland.carland_service.dto.response.v2.LineUpdateDetail;
import com.carland.carland_service.dto.response.v2.MoneyResponse;
import com.carland.carland_service.dto.response.v2.PartUpdateDetail;
import com.carland.carland_service.dto.response.v2.PartnerUpdateServiceVisitResult;
import com.carland.carland_service.dto.response.v2.ServiceHistoryLineV2Response;
import com.carland.carland_service.dto.response.v2.ServiceHistoryPartV2;
import com.carland.carland_service.dto.response.v2.ServiceHistoryPartV2Response;
import com.carland.carland_service.dto.response.v2.ServiceHistoryV2;
import com.carland.carland_service.dto.response.v2.Visit;
import com.carland.carland_service.entity.Car;
import com.carland.carland_service.entity.Partner;
import com.carland.carland_service.enums.EnumPartnerId;
import com.carland.carland_service.exceptions.MissingFieldException;
import com.carland.carland_service.exceptions.ResourceNotFoundException;
import com.carland.carland_service.repository.CarRepository;
import com.carland.carland_service.repository.VisitRepository;
import com.carland.carland_service.service.HyperPercentageSyncService;
import com.carland.carland_service.service.PartnerLookupService;
import com.carland.carland_service.service.PartnerServiceVisitUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PartnerServiceVisitUpdateServiceImpl implements PartnerServiceVisitUpdateService {

    private static final EnumPartnerId DEFAULT_PARTNER = EnumPartnerId.HYPER;

    private final CarRepository carRepository;
    private final VisitRepository visitRepository;
    private final PartnerLookupService partnerLookupService;
    private final HyperPercentageSyncService hyperPercentageSyncService;

    @Override
    @Transactional
    public PartnerUpdateServiceVisitResult update(PartnerUpdateServiceVisitRequest request) {
        validateRequest(request);

        String vin = request.getVin().trim();
        Car car = carRepository.findByVin(vin);
        if (car == null) {
            throw new ResourceNotFoundException("Car not found for vin: " + vin);
        }

        Visit visit = visitRepository.findWithDetailsByCarIdAndHyperRecordId(car.getCarId(), request.getPartnerRecordId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Visit not found for partnerRecordId=" + request.getPartnerRecordId() + " and vin=" + vin));

        visit.getParts().size();

        PartnerUpdateServiceVisitResult result = PartnerUpdateServiceVisitResult.builder()
                .vin(vin)
                .partnerRecordId(request.getPartnerRecordId())
                .visitId(visit.getId())
                .lines(new ArrayList<>())
                .parts(new ArrayList<>())
                .build();

        applyVisitUpdates(visit, request);
        result.setLinesUpdated(updateLines(visit, request, result));
        result.setPartsUpdated(updateParts(visit, request, result));

        visitRepository.saveAndFlush(visit);
        recalculateAllTimeCost(car);
        hyperPercentageSyncService.syncFromVisits(car, visitRepository.findAllByCarOrderByLastServiceDateDescIdDesc(car));

        result.setMessage("Visit and service lines updated");
        return result;
    }

    private void validateRequest(PartnerUpdateServiceVisitRequest request) {
        if (request == null) {
            throw new MissingFieldException("request body is required");
        }
        if (!StringUtils.hasText(request.getVin())) {
            throw new MissingFieldException("vin is required");
        }
        if (request.getPartnerRecordId() == null) {
            throw new MissingFieldException("partnerRecordId is required");
        }
        if (!hasVisitUpdates(request) && !hasLineUpdates(request) && !hasPartUpdates(request)) {
            throw new MissingFieldException("At least one visit, service line or part field must be provided for update");
        }
    }

    private boolean hasVisitUpdates(PartnerUpdateServiceVisitRequest request) {
        return request.getType() != null
                || request.getDate() != null
                || request.getMileage() != null
                || request.getServiceCenterId() != null
                || StringUtils.hasText(request.getServiceCenterName())
                || StringUtils.hasText(request.getDealer())
                || request.getAmount() != null
                || (request.getServiceGroups() != null && !request.getServiceGroups().isEmpty());
    }

    private boolean hasLineUpdates(PartnerUpdateServiceVisitRequest request) {
        return request.getServices() != null && !request.getServices().isEmpty();
    }

    private boolean hasPartUpdates(PartnerUpdateServiceVisitRequest request) {
        return request.getParts() != null && !request.getParts().isEmpty();
    }

    private void applyVisitUpdates(Visit visit, PartnerUpdateServiceVisitRequest request) {
        if (request.getType() != null) {
            visit.setServiceType(request.getType());
        }
        if (request.getDate() != null) {
            visit.setLastServiceDate(request.getDate());
        }
        if (request.getMileage() != null) {
            visit.setLastServiceMileage(request.getMileage());
        }
        if (request.getDealer() != null) {
            visit.setDealer(request.getDealer());
        }
        if (request.getServiceGroups() != null) {
            visit.setServiceGroups(new ArrayList<>(request.getServiceGroups()));
        }
        if (request.getServiceCenterId() != null || StringUtils.hasText(request.getServiceCenterName())) {
            Long partnerId = request.getServiceCenterId() != null ? request.getServiceCenterId() : visit.getServiceCenterId();
            if (partnerId == null) {
                partnerId = DEFAULT_PARTNER.getId();
            }
            visit.setServiceCenterId(partnerId);
            visit.setServiceCenterName(resolvePartnerName(request, partnerId, visit.getServiceCenterName()));
        }
        if (request.getAmount() != null) {
            visit.setFinalCostAmount(request.getAmount().getAmount());
            visit.setFinalCostCurrency(request.getAmount().getCurrency());
        }
    }

    private int updateLines(Visit visit, PartnerUpdateServiceVisitRequest request, PartnerUpdateServiceVisitResult result) {
        if (!hasLineUpdates(request)) {
            return 0;
        }

        int updated = 0;
        for (ServiceHistoryLineV2Response lineRequest : request.getServices()) {
            if (lineRequest.getServiceCode() == null) {
                throw new MissingFieldException("serviceCode is required for each service line update");
            }

            ServiceHistoryV2 existingLine = findLineByServiceCode(visit, lineRequest.getServiceCode());
            if (existingLine == null) {
                throw new ResourceNotFoundException(
                        "Service line not found for serviceCode=" + lineRequest.getServiceCode()
                                + " in visit partnerRecordId=" + request.getPartnerRecordId());
            }

            applyLineUpdates(existingLine, lineRequest);
            updated++;

            result.getLines().add(LineUpdateDetail.builder()
                    .serviceCode(existingLine.getServiceCode())
                    .lineId(existingLine.getId())
                    .updated(true)
                    .build());
        }
        return updated;
    }

    private int updateParts(Visit visit, PartnerUpdateServiceVisitRequest request, PartnerUpdateServiceVisitResult result) {
        if (!hasPartUpdates(request)) {
            return 0;
        }

        int updated = 0;
        for (ServiceHistoryPartV2Response partRequest : request.getParts()) {
            ServiceHistoryPartV2 existingPart = findPart(visit, partRequest, request.getPartnerRecordId());
            applyPartUpdates(existingPart, partRequest);
            updated++;

            result.getParts().add(PartUpdateDetail.builder()
                    .name(existingPart.getName())
                    .qty(existingPart.getQty())
                    .unit(existingPart.getUnit())
                    .partId(existingPart.getId())
                    .updated(true)
                    .build());
        }
        return updated;
    }

    private void applyLineUpdates(ServiceHistoryV2 target, ServiceHistoryLineV2Response source) {
        if (source.getServiceName() != null) {
            target.setServiceName(source.getServiceName());
        }
        if (source.getUniversalServiceId() != null) {
            target.setUniversalServiceId(normalizeUniversalServiceId(source.getUniversalServiceId()));
        }
        if (source.getCost() != null) {
            MoneyResponse cost = source.getCost();
            target.setCostAmount(cost.getAmount());
            target.setCostCurrency(cost.getCurrency());
        }
        if (source.getNextServiceDate() != null) {
            target.setNextServiceDate(source.getNextServiceDate());
        }
        if (source.getNextServiceMileage() != null) {
            target.setNextServiceMileage(source.getNextServiceMileage());
        }
    }

    private void applyPartUpdates(ServiceHistoryPartV2 target, ServiceHistoryPartV2Response source) {
        if (source.getName() != null) {
            target.setName(source.getName());
        }
        if (source.getQty() != null) {
            target.setQty(source.getQty());
        }
        if (source.getUnit() != null) {
            target.setUnit(source.getUnit());
        }
    }

    private ServiceHistoryV2 findLineByServiceCode(Visit visit, Integer serviceCode) {
        return visit.getServices().stream()
                .filter(line -> Objects.equals(line.getServiceCode(), serviceCode))
                .findFirst()
                .orElse(null);
    }

    private ServiceHistoryPartV2 findPart(Visit visit, ServiceHistoryPartV2Response partRequest, Long partnerRecordId) {
        if (partRequest.getId() != null) {
            return visit.getParts().stream()
                    .filter(part -> Objects.equals(part.getId(), partRequest.getId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Part not found for partId=" + partRequest.getId()
                                    + " in visit partnerRecordId=" + partnerRecordId));
        }

        validatePartIdentity(partRequest);
        ServiceHistoryPartV2 existingPart = findPartByIdentity(visit, partRequest);
        if (existingPart == null) {
            throw new ResourceNotFoundException(
                    "Part not found for name=" + partRequest.getName()
                            + ", qty=" + normalizeQty(partRequest.getQty())
                            + ", unit=" + partRequest.getUnit()
                            + " in visit partnerRecordId=" + partnerRecordId);
        }
        return existingPart;
    }

    private ServiceHistoryPartV2 findPartByIdentity(Visit visit, ServiceHistoryPartV2Response partRequest) {
        String key = partIdentityKey(partRequest.getName(), partRequest.getQty(), partRequest.getUnit());
        return visit.getParts().stream()
                .filter(part -> partIdentityKey(part.getName(), part.getQty(), part.getUnit()).equals(key))
                .findFirst()
                .orElse(null);
    }

    private void validatePartIdentity(ServiceHistoryPartV2Response partRequest) {
        if (!StringUtils.hasText(partRequest.getName()) || partRequest.getQty() == null || !StringUtils.hasText(partRequest.getUnit())) {
            throw new MissingFieldException("name, qty and unit are required to identify each part update");
        }
    }

    private String partIdentityKey(String name, BigDecimal qty, String unit) {
        return Objects.toString(name, "")
                + "|" + normalizeQty(qty)
                + "|" + Objects.toString(unit, "");
    }

    private String normalizeQty(BigDecimal qty) {
        if (qty == null) {
            return "";
        }
        return qty.stripTrailingZeros().toPlainString();
    }

    private String normalizeUniversalServiceId(String raw) {
        if (raw == null || raw.isBlank() || "other".equalsIgnoreCase(raw.trim())) {
            return "";
        }
        return raw.trim();
    }

    private String resolvePartnerName(PartnerUpdateServiceVisitRequest request, Long partnerId, String currentName) {
        if (StringUtils.hasText(request.getServiceCenterName())) {
            return request.getServiceCenterName();
        }
        if (StringUtils.hasText(currentName)) {
            return currentName;
        }
        return partnerLookupService.find(EnumPartnerId.fromId(partnerId).orElse(DEFAULT_PARTNER))
                .map(Partner::getName)
                .orElse(DEFAULT_PARTNER.getDefaultName());
    }

    private void recalculateAllTimeCost(Car car) {
        BigDecimal total = visitRepository.findAllByCarOrderByLastServiceDateDescIdDesc(car).stream()
                .map(visit -> visit.getFinalCostAmount() != null ? visit.getFinalCostAmount()
                        : visit.getCostAmount() != null ? visit.getCostAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        car.setAllTimeCost(total);
        carRepository.save(car);
    }
}

package com.carland.carland_service.service;

import com.carland.carland_service.dto.response.v2.ServiceHistoryV2;
import com.carland.carland_service.dto.response.v2.Visit;
import com.carland.carland_service.entity.Car;
import com.carland.carland_service.entity.Percentage;
import com.carland.carland_service.enums.HyperServiceMapping;
import com.carland.carland_service.enums.PercentageStatus;
import com.carland.carland_service.repository.PercentageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Applies Hyper (partner) service history onto a car's percentages.
 *
 * <p>Rules:</p>
 * <ul>
 *   <li>Match: Hyper {@code universalServiceId} (String) is translated to our serviceName
 *       via {@link HyperServiceMapping}; the percentage is found by serviceName.</li>
 *   <li>Precedence: partner data is authoritative. CREATED and EDITED_BY_CUSTOMER are
 *       overwritten and become {@link PercentageStatus#EDITED_BY_PARTNER} (locked).
 *       An already partner-locked row is only updated by a strictly newer Hyper record.</li>
 *   <li>Idempotent: re-applying the same Hyper record is a no-op.</li>
 *   <li>Unmatched / never-serviced records are skipped silently (no error).</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HyperPercentageSyncService {

    private final PercentageRepository percentageRepository;

    public void syncFromVisits(Car car, List<Visit> visits) {
        if (visits == null || visits.isEmpty()) {
            return;
        }

        List<Percentage> percentages = percentageRepository.findAllByCarId(car.getCarId());
        for (Percentage percentage : percentages) {
            if (percentage.getServiceName() == null || percentage.getServiceName().isBlank()) {
                continue;
            }

            Optional<HyperServiceMatch> matchOpt = findLatestMatch(percentage.getServiceName(), visits);
            if (matchOpt.isEmpty()) {
                continue;
            }
            HyperServiceMatch match = matchOpt.get();

            if (!hasUsableData(match)) {
                // never serviced / nothing to set -> skip silently
                continue;
            }

            PercentageStatus current = PercentageStatus.fromStored(percentage.getStatus());
            String matchedRecordId = recordIdOf(match.visit());

            if (current == PercentageStatus.EDITED_BY_PARTNER) {
                boolean sameRecord = matchedRecordId != null
                        && matchedRecordId.equals(percentage.getPartnerRecordId());
                if (sameRecord || !isNewerThanApplied(match.visit(), percentage)) {
                    continue; // already applied or not newer -> idempotent no-op
                }
            }

            applyPartnerData(percentage, match, matchedRecordId);
            percentageRepository.save(percentage);
            log.info("Synced percentage from Hyper | carId={}, serviceName={}, percentageId={}, recordId={}",
                    car.getCarId(), percentage.getServiceName(), percentage.getId(), matchedRecordId);
        }
    }

    private Optional<HyperServiceMatch> findLatestMatch(String serviceName, List<Visit> visits) {
        HyperServiceMatch best = null;

        for (Visit visit : visits) {
            if (visit.getServices() == null) {
                continue;
            }
            for (ServiceHistoryV2 line : visit.getServices()) {
                Optional<String> mappedName = HyperServiceMapping.toServiceName(line.getUniversalServiceId());
                if (mappedName.isEmpty() || !mappedName.get().equalsIgnoreCase(serviceName)) {
                    continue;
                }
                if (best == null || isNewerVisit(visit, best.visit())) {
                    best = new HyperServiceMatch(visit, line);
                }
            }
        }

        return Optional.ofNullable(best);
    }

    private boolean hasUsableData(HyperServiceMatch match) {
        Visit visit = match.visit();
        ServiceHistoryV2 line = match.line();
        return visit.getLastServiceDate() != null
                || visit.getLastServiceMileage() != null
                || line.getNextServiceDate() != null
                || line.getNextServiceMileage() != null;
    }

    private boolean isNewerVisit(Visit candidate, Visit currentBest) {
        LocalDate candidateDate = candidate.getLastServiceDate();
        LocalDate bestDate = currentBest.getLastServiceDate();

        if (candidateDate == null && bestDate == null) {
            return compareById(candidate, currentBest) > 0;
        }
        if (candidateDate == null) {
            return false;
        }
        if (bestDate == null) {
            return true;
        }

        int dateCompare = candidateDate.compareTo(bestDate);
        if (dateCompare != 0) {
            return dateCompare > 0;
        }
        return compareById(candidate, currentBest) > 0;
    }

    private int compareById(Visit a, Visit b) {
        if (a.getId() == null || b.getId() == null) {
            return 0;
        }
        return Long.compare(a.getId(), b.getId());
    }

    /** True when the matched visit is strictly newer than what is currently stored on the percentage. */
    private boolean isNewerThanApplied(Visit visit, Percentage percentage) {
        LocalDate visitDate = visit.getLastServiceDate();
        LocalDate appliedDate = percentage.getLastServiceDate();

        if (visitDate == null) {
            return false;
        }
        if (appliedDate == null) {
            return true;
        }
        int cmp = visitDate.compareTo(appliedDate);
        if (cmp != 0) {
            return cmp > 0;
        }
        Integer visitKm = visit.getLastServiceMileage();
        Integer appliedKm = percentage.getLastServiceKm();
        if (visitKm == null) {
            return false;
        }
        return appliedKm == null || visitKm > appliedKm;
    }

    private void applyPartnerData(Percentage percentage, HyperServiceMatch match, String matchedRecordId) {
        Visit visit = match.visit();
        ServiceHistoryV2 line = match.line();

        if (visit.getLastServiceDate() != null) {
            percentage.setLastServiceDate(visit.getLastServiceDate());
        }
        if (visit.getLastServiceMileage() != null) {
            percentage.setLastServiceKm(visit.getLastServiceMileage());
        }
        if (line.getNextServiceDate() != null) {
            percentage.setNextServiceDate(line.getNextServiceDate());
        }
        if (line.getNextServiceMileage() != null) {
            percentage.setNextServiceKm(line.getNextServiceMileage());
        }

        percentage.setStatus(PercentageStatus.EDITED_BY_PARTNER.name());
        percentage.setPartnerRecordId(matchedRecordId);
        percentage.setLastPartnerSyncAt(LocalDateTime.now());
    }

    private String recordIdOf(Visit visit) {
        return visit.getHyperRecordId() != null ? String.valueOf(visit.getHyperRecordId()) : null;
    }

    private record HyperServiceMatch(Visit visit, ServiceHistoryV2 line) {
    }
}

package com.carland.carland_service.service;

import com.carland.carland_service.dto.response.v2.ServiceHistoryV2;
import com.carland.carland_service.dto.response.v2.Visit;
import com.carland.carland_service.entity.Car;
import com.carland.carland_service.entity.Percentage;
import com.carland.carland_service.repository.PercentageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class HyperPercentageSyncService {

    private static final String EDITED_STATUS = "EDITED";

    private final PercentageRepository percentageRepository;

    public void syncFromVisits(Car car, List<Visit> visits) {
        if (visits == null || visits.isEmpty()) {
            return;
        }

        List<Percentage> percentages = percentageRepository.findAllByCarId(car.getCarId());
        for (Percentage percentage : percentages) {
            if (EDITED_STATUS.equalsIgnoreCase(percentage.getStatus())) {
                continue;
            }
            if (percentage.getServiceId() == null) {
                continue;
            }

            findLatestMatch(percentage.getServiceId(), visits).ifPresent(match -> {
                if (applyMatch(percentage, match)) {
                    percentageRepository.save(percentage);
                    log.info("Synced percentage from Hyper | carId={}, serviceId={}, percentageId={}",
                            car.getCarId(), percentage.getServiceId(), percentage.getId());
                }
            });
        }
    }

    private java.util.Optional<HyperServiceMatch> findLatestMatch(Long serviceId, List<Visit> visits) {
        HyperServiceMatch best = null;

        for (Visit visit : visits) {
            if (visit.getServices() == null) {
                continue;
            }
            for (ServiceHistoryV2 line : visit.getServices()) {
                if (!Objects.equals(serviceId, line.getUniversalServiceId())) {
                    continue;
                }
                if (best == null || isNewerVisit(visit, best.visit())) {
                    best = new HyperServiceMatch(visit, line);
                }
            }
        }

        return java.util.Optional.ofNullable(best);
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

    private boolean applyMatch(Percentage percentage, HyperServiceMatch match) {
        boolean changed = false;
        Visit visit = match.visit();
        ServiceHistoryV2 line = match.line();

        LocalDate hyperLastDate = visit.getLastServiceDate();
        if (hyperLastDate != null
                && (percentage.getLastServiceDate() == null || hyperLastDate.isAfter(percentage.getLastServiceDate()))) {
            percentage.setLastServiceDate(hyperLastDate);
            changed = true;

            Integer hyperLastKm = visit.getLastServiceMileage();
            if (hyperLastKm != null) {
                percentage.setLastServiceKm(hyperLastKm);
            }
        }

        LocalDate hyperNextDate = line.getNextServiceDate();
        if (hyperNextDate != null) {
            LocalDate currentNextDate = percentage.getNextServiceDate();
            if (currentNextDate == null || currentNextDate.isAfter(hyperNextDate)) {
                percentage.setNextServiceDate(hyperNextDate);
                changed = true;
            }
        }

        Integer hyperNextKm = line.getNextServiceMileage();
        if (hyperNextKm != null) {
            Integer currentNextKm = percentage.getNextServiceKm();
            if (currentNextKm == null || currentNextKm > hyperNextKm) {
                percentage.setNextServiceKm(hyperNextKm);
                changed = true;
            }
        }

        return changed;
    }

    private record HyperServiceMatch(Visit visit, ServiceHistoryV2 line) {
    }
}

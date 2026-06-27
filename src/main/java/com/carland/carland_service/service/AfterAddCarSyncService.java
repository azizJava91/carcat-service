package com.carland.carland_service.service;

import com.carland.carland_service.dto.response.v2.CarVinHistoryServiceV2;
import com.carland.carland_service.service.interfaces.CarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Runs the percentage calculation + Hyper partner sync AFTER a car is added.
 *
 * <p>This is intentionally decoupled from addCar: it runs asynchronously and is fully
 * best-effort. Hyper being down or slow must never affect the addCar response; in that
 * case the percentages simply stay CREATED.</p>
 */
@Service
@Slf4j
public class AfterAddCarSyncService {

    private final CarService carService;
    private final CarVinHistoryServiceV2 carVinHistoryServiceV2;

    public AfterAddCarSyncService(@Lazy CarService carService,
                                  CarVinHistoryServiceV2 carVinHistoryServiceV2) {
        this.carService = carService;
        this.carVinHistoryServiceV2 = carVinHistoryServiceV2;
    }

    @Async
    public void syncAfterAddCar(Long carId,
                                String vin,
                                String phoneNumber,
                                String userIdHeader,
                                String timezone,
                                String acceptLanguage) {
        // 1) Calculate + persist percentages (independent of Hyper).
        try {
            carService.executeServicePercentages(carId, phoneNumber, userIdHeader, timezone, acceptLanguage);
        } catch (Exception e) {
            log.warn("[afterAddCar] executeServicePercentages failed | carId={}, reason={}", carId, e.getMessage());
        }

        // 2) Pull Hyper history + apply partner data (idempotent, error-isolated).
        try {
            carVinHistoryServiceV2.getServiceHistoryByVin(vin, phoneNumber, userIdHeader, acceptLanguage);
        } catch (Exception e) {
            log.warn("[afterAddCar] Hyper sync failed (percentages stay CREATED) | carId={}, vin={}, reason={}",
                    carId, vin, e.getMessage());
        }
    }
}

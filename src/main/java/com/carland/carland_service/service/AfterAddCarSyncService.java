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
        log.info("[pct-status-debug] afterAddCar async START | carId={}, vin={}, thread={}",
                carId, vin, Thread.currentThread().getName());

        // 1) Calculate + persist percentages (independent of Hyper).
        try {
            log.info("[pct-status-debug] afterAddCar calling executeServicePercentages (async) | carId={}", carId);
            carService.executeServicePercentages(carId, phoneNumber, userIdHeader, timezone, acceptLanguage);
            log.info("[pct-status-debug] afterAddCar executeServicePercentages finished (async) | carId={}", carId);
        } catch (Exception e) {
            log.warn("[pct-status-debug] afterAddCar executeServicePercentages failed | carId={}, reason={}", carId, e.getMessage());
        }

        // 2) Pull Hyper history + apply partner data (idempotent, error-isolated).
        try {
            log.info("[pct-status-debug] afterAddCar calling Hyper sync (async) | carId={}, vin={}", carId, vin);
            carVinHistoryServiceV2.getServiceHistoryByVin(vin, phoneNumber, userIdHeader, acceptLanguage);
            log.info("[pct-status-debug] afterAddCar Hyper sync finished (async) | carId={}, vin={}", carId, vin);
        } catch (Exception e) {
            log.warn("[pct-status-debug] afterAddCar Hyper sync failed | carId={}, vin={}, reason={}",
                    carId, vin, e.getMessage());
        }

        log.info("[pct-status-debug] afterAddCar async END | carId={}, vin={}", carId, vin);
    }
}

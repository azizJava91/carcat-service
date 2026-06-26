package com.carland.carland_service.controller;

import com.carland.carland_service.dto.response.v2.CarVinServiceHistoryV2Response;
import com.carland.carland_service.service.interfaces.CarVinHistoryV2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/car")
@RequiredArgsConstructor
public class CarV2Controller {

    private final CarVinHistoryV2Service carVinHistoryV2Service;

    @GetMapping("/{vin}/service-history")
    public CarVinServiceHistoryV2Response getServiceHistoryByVin(@PathVariable String vin,
                                                                  @RequestHeader("phoneNumber") String phoneNumber,
                                                                  @RequestHeader("X-User-Id") String userIdHeader,
                                                                  @RequestHeader("Accept-Language") String acceptLanguage) {
        return carVinHistoryV2Service.getServiceHistoryByVin(vin, phoneNumber, userIdHeader, acceptLanguage);
    }
}

package com.carland.carland_service.controller;

import com.carland.carland_service.dto.request.BulkRequest;
import com.carland.carland_service.dto.request.DeviceTokenRequest;
import com.carland.carland_service.dto.response.BulkResponse;
import com.carland.carland_service.dto.response.DeviceResponse;
import com.carland.carland_service.entity.Customer;
import com.carland.carland_service.service.interfaces.DeviceTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/device-tokens")
@RequiredArgsConstructor
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    @PostMapping("/post")
    public DeviceResponse saveOrUpdateToken(@RequestBody DeviceTokenRequest request) {
        return deviceTokenService.saveOrUpdateToken(request);

    }

    @PostMapping("/send/bulk")
    public BulkResponse sendBulk(@RequestBody BulkRequest bulkRequest,
                                 @RequestHeader("Accept-Language") String acceptLanguage){
        return deviceTokenService.sendBulk(bulkRequest, acceptLanguage);
    }
}

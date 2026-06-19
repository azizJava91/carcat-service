package com.carland.carland_service.controller;

import com.carland.carland_service.dto.request.AutoServiceRequest;
import com.carland.carland_service.dto.request.ServiceHistoryRequest;
import com.carland.carland_service.dto.request.ServiceRequest;
import com.carland.carland_service.dto.response.AutoServiceResponse;
import com.carland.carland_service.dto.response.ServiceHistoryResponse;
import com.carland.carland_service.dto.response.ServiceResponse;
import com.carland.carland_service.service.interfaces.AutoServiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auto-service")
@RequiredArgsConstructor
@Slf4j
public class AutoServiceController {
    private final AutoServiceService autoServiceService;

    @PostMapping("/create")
    public AutoServiceResponse createAutoService(@RequestBody AutoServiceRequest autoServiceRequest,
                                                 @RequestHeader("Authorization") String token,
                                                 @RequestHeader("phoneNumber") String phoneNumber,
                                                 @RequestHeader("role") String role,
                                                 @RequestHeader("X-User-Id") String userIdHeader,
                                                 @RequestHeader("X-Client-Timezone") String timezone,
                                                 @RequestHeader("Accept-Language") String acceptLanguage) {


        return autoServiceService.createAutoService(autoServiceRequest, phoneNumber, role, userIdHeader, timezone, acceptLanguage);
    }

    @PostMapping("/insert/service/history")

    public ServiceHistoryResponse insertServiceHistory(@RequestHeader("Authorization") String token,
                                                       @RequestBody ServiceHistoryRequest request,
                                                       @RequestHeader("phoneNumber") String phoneNumber,
                                                       @RequestHeader("X-User-Id") String userIdHeader,
                                                       @RequestHeader("role") String role,
                                                       @RequestHeader("X-Client-Timezone") String timezone,
                                                       @RequestHeader("Accept-Language") String acceptLanguage) {
        return autoServiceService.insertServiceHistory(request, phoneNumber, userIdHeader, role, timezone, acceptLanguage);
    }

    @GetMapping("/get/service")
    public ServiceResponse getService(@RequestHeader("Authorization") String token,
                                      @RequestBody ServiceRequest request,
                                      @RequestHeader("phoneNumber") String phoneNumber,
                                      @RequestHeader("X-User-Id") String userIdHeader,
                                      @RequestHeader("X-Client-Timezone") String timezone,
                                      @RequestHeader("Accept-Language") String acceptLanguage) {
        return autoServiceService.getService(request, phoneNumber, userIdHeader, timezone, acceptLanguage);
    }

    @PostMapping("/add/service/amount")
    public ServiceResponse addServiceAmount(@RequestHeader("Authorization") String token,
                                            @RequestBody ServiceRequest request,
                                            @RequestHeader("phoneNumber") String phoneNumber,
                                            @RequestHeader("X-User-Id") String userIdHeader,
                                            @RequestHeader("role") String role,
                                            @RequestHeader("X-Client-Timezone") String timezone,
                                            @RequestHeader("Accept-Language") String acceptLanguage) {
        return autoServiceService.addServiceAmount(request, phoneNumber, userIdHeader, role, timezone, acceptLanguage);
    }
}

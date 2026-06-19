package com.carland.carland_service.controller;

import com.carland.carland_service.dto.request.MaintenanceTemplateRequest;
import com.carland.carland_service.dto.request.ServiceRequest;
import com.carland.carland_service.dto.response.MaintenanceTemplateResponse;
import com.carland.carland_service.service.interfaces.MaintenanceTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/template")
@RequiredArgsConstructor

public class MaintenanceTemplateController {

    private final MaintenanceTemplateService maintenanceTemplateService;

    @PostMapping("/create")
    public MaintenanceTemplateResponse createMaintenanceTemplate(@RequestBody MaintenanceTemplateRequest maintenanceTemplateRequest,
                                                                 @RequestHeader("Authorization") String token,
                                                                 @RequestHeader("role") String role,
                                                                 @RequestHeader("phoneNumber") String phoneNumber,
                                                                 @RequestHeader("X-User-Id") String userIdHeader,
                                                                 @RequestHeader("X-Client-Timezone") String timezone,
                                                                 @RequestHeader("Accept-Language") String acceptLanguage) {
        return maintenanceTemplateService.createMaintenanceTemplate(maintenanceTemplateRequest, role, phoneNumber,
                userIdHeader, timezone, acceptLanguage);
    }

    @GetMapping("/list")
    public List<MaintenanceTemplateResponse> getMaintenanceTemplateList(@RequestHeader("Authorization") String token,
                                                                        @RequestHeader("phoneNumber") String phoneNumber,
                                                                        @RequestHeader("X-User-Id") String userIdHeader,
                                                                        @RequestHeader("X-Client-Timezone") String timezone,
                                                                        @RequestHeader("Accept-Language") String acceptLanguage) {
        return maintenanceTemplateService.getMaintenanceTemplateList(phoneNumber, userIdHeader, timezone, acceptLanguage);
    }

    @PostMapping("/add/service")
    public MaintenanceTemplateResponse addServiceToTemplate(@RequestHeader("Authorization") String token,
                                                            @RequestParam Long templateId,
                                                            @RequestBody ServiceRequest request,
                                                            @RequestHeader("phoneNumber") String phoneNumber,
                                                            @RequestHeader("X-User-Id") String userIdHeader,
                                                            @RequestHeader("role") String role,
                                                            @RequestHeader("X-Client-Timezone") String timezone,
                                                            @RequestHeader("Accept-Language") String acceptLanguage){
        return maintenanceTemplateService.addServiceToTemplate(templateId, request, phoneNumber, userIdHeader, role, timezone, acceptLanguage);

    }
}

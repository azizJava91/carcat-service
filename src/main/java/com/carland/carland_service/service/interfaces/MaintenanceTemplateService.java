package com.carland.carland_service.service.interfaces;

import com.carland.carland_service.dto.request.MaintenanceTemplateRequest;
import com.carland.carland_service.dto.request.ServiceRequest;
import com.carland.carland_service.dto.response.MaintenanceTemplateResponse;

import java.util.List;

public interface MaintenanceTemplateService {
    MaintenanceTemplateResponse createMaintenanceTemplate(MaintenanceTemplateRequest maintenanceTemplateRequest,
                                                          String role, String phoneNumber, String userIdHeader,
                                                          String timezone, String acceptLanguage);

    List<MaintenanceTemplateResponse> getMaintenanceTemplateList(String phoneNumber, String userIdHeader,
                                                                 String timezone, String acceptLanguage);


    MaintenanceTemplateResponse addServiceToTemplate(Long templateId, ServiceRequest request, String phoneNumber,
                                                     String userIdHeader, String roel, String timezone, String acceptLanguage);
}

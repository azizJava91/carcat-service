package com.carland.carland_service.service.interfaces;

import com.carland.carland_service.dto.request.SuperAdminRequest;
import com.carland.carland_service.dto.response.SuperAdminResponse;

public interface SuperAdminService {
    SuperAdminResponse createSuperAdmin(SuperAdminRequest superAdminRequest, String phoneNumber, String userIdHeader, String role, String timezone, String acceptLanguage);
}

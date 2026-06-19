package com.carland.carland_service.service.impl;

import com.carland.carland_service.dto.request.SuperAdminRequest;
import com.carland.carland_service.dto.response.SuperAdminResponse;
import com.carland.carland_service.service.interfaces.SuperAdminService;
import org.springframework.stereotype.Service;


@Service
public class SuperAdminServiceImpl implements SuperAdminService {
    @Override
    public SuperAdminResponse createSuperAdmin(SuperAdminRequest superAdminRequest, String phoneNumber, String userIdHeader, String role, String timezone, String acceptLanguage) {
        return null;
    }
}

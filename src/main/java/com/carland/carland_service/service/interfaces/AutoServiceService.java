package com.carland.carland_service.service.interfaces;

import com.carland.carland_service.dto.request.AutoServiceRequest;
import com.carland.carland_service.dto.request.ServiceHistoryRequest;
import com.carland.carland_service.dto.request.ServiceRequest;
import com.carland.carland_service.dto.response.AutoServiceResponse;
import com.carland.carland_service.dto.response.ServiceHistoryResponse;
import com.carland.carland_service.dto.response.ServiceResponse;

public interface AutoServiceService {
    AutoServiceResponse createAutoService(AutoServiceRequest autoServiceRequest, String phoneNumber, String role,String userIdHeader, String timezone, String acceptLanguage);


    ServiceHistoryResponse insertServiceHistory(ServiceHistoryRequest request, String phoneNumber, String userIdHeader, String role, String timezone, String acceptLanguage);

    ServiceResponse getService(ServiceRequest request, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage);


    ServiceResponse addServiceAmount(ServiceRequest request, String phoneNumber, String userIdHeader, String role, String timezone, String acceptLanguage);

}

package com.carland.carland_service.service.interfaces;

import com.carland.carland_service.dto.response.CarVinServiceHistoryResponse;

public interface CarVinHistoryService {
    CarVinServiceHistoryResponse getServiceHistoryByVin(String vin, String phoneNumber, String userIdHeader, String acceptLanguage);
}

package com.carland.carland_service.service.interfaces;

import com.carland.carland_service.dto.response.v2.CarVinServiceHistoryV2Response;

public interface CarVinHistoryV2Service {
    CarVinServiceHistoryV2Response getServiceHistoryByVin(String vin, String phoneNumber, String userIdHeader, String acceptLanguage);
}

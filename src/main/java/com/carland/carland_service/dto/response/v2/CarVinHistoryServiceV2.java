package com.carland.carland_service.dto.response.v2;

import com.carland.carland_service.dto.response.v2.CarVinServiceHistoryV2Response;

public interface CarVinHistoryServiceV2 {
    CarVinServiceHistoryV2Response getServiceHistoryByVin(String vin, String phoneNumber, String userIdHeader, String acceptLanguage);
}

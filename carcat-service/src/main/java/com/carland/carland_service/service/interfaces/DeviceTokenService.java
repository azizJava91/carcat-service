package com.carland.carland_service.service.interfaces;


import com.carland.carland_service.dto.request.BulkRequest;
import com.carland.carland_service.dto.request.DeviceTokenRequest;
import com.carland.carland_service.dto.response.BulkResponse;
import com.carland.carland_service.dto.response.DeviceResponse;

public interface DeviceTokenService {
    DeviceResponse saveOrUpdateToken(DeviceTokenRequest request);

    BulkResponse sendBulk(BulkRequest bulkRequest, String acceptLanguage);
}

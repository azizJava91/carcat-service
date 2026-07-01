package com.carland.carland_service.service;

import com.carland.carland_service.dto.request.PartnerUpdateServiceVisitRequest;
import com.carland.carland_service.dto.response.v2.PartnerUpdateServiceVisitResult;

public interface PartnerServiceVisitUpdateService {
    PartnerUpdateServiceVisitResult update(PartnerUpdateServiceVisitRequest request);
}

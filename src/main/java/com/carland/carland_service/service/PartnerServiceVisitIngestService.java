package com.carland.carland_service.service;

import com.carland.carland_service.dto.response.v2.CarVinServiceHistoryV2Response;
import com.carland.carland_service.dto.response.v2.PartnerNewServiceVisitResult;

public interface PartnerServiceVisitIngestService {

    PartnerNewServiceVisitResult ingest(CarVinServiceHistoryV2Response request);
}

package com.carland.carland_service.service;

import com.carland.carland_service.dto.response.hyper.HyperVehicleByVinResponse;
import com.carland.carland_service.dto.response.v2.PartnerNewServiceVisitResult;

public interface PartnerServiceVisitIngestService {

    PartnerNewServiceVisitResult ingest(HyperVehicleByVinResponse request);
}

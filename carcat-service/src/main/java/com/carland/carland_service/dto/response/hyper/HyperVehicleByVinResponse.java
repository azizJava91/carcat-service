package com.carland.carland_service.dto.response.hyper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HyperVehicleByVinResponse {
    private String vin;
    private List<HyperServiceHistoryItemResponse> serviceHistory;
}

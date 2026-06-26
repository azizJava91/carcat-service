package com.carland.carland_service.dto.response.hyper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HyperVehicleByVinResponse {
    private String plate;
    private String vin;
    private String brand;
    private String model;
    private Integer year;
    private Double engineVolume;
    private String engineType;
    private String bodyType;
    private String trim;
    private Integer currentMileage;
    private List<HyperServiceHistoryItemResponse> serviceHistory;
}

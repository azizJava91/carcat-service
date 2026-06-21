package com.carland.carland_service.dto.response.hyper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HyperServiceHistoryItemResponse {
    private String serviceType;
    private List<String> serviceGroups;
    private LocalDate lastServiceDate;
    private Integer lastServiceMileage;
    private List<HyperServicePartResponse> parts;
    private HyperCostResponse finalCost;
    private LocalDate nextServiceDate;
    private Integer nextServiceMileage;
    private String dealer;
}

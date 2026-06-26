package com.carland.carland_service.dto.response.hyper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HyperServiceLineResponse {
    private Integer serviceCode;
    private String serviceName;
    private List<String> serviceGroups;
    private String universalServiceId;
    private HyperCostResponse cost;
    private LocalDate nextServiceDate;
    private Integer nextServiceMileage;
}

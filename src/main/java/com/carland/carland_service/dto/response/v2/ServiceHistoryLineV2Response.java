package com.carland.carland_service.dto.response.v2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceHistoryLineV2Response {
    private Integer serviceCode;
    private String universalServiceId;
    private String serviceName;
    private List<String> serviceGroups;
    private MoneyResponse cost;
    private LocalDate nextServiceDate;
    private Integer nextServiceMileage;
}

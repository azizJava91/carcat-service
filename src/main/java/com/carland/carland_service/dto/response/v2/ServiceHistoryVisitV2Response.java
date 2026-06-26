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
public class ServiceHistoryVisitV2Response {
    private Long id;
    private Long partnerRecordId;
    private String type;
    private List<String> serviceGroups;
    private List<ServiceHistoryLineV2Response> services;
    private LocalDate date;
    private Integer mileage;
    private Long serviceCenterId;
    private String serviceCenterName;
    private String dealer;
    private MoneyResponse amount;
    private List<ServiceHistoryPartV2Response> parts;
}

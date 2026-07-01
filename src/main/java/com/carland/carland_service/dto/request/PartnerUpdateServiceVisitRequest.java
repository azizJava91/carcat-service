package com.carland.carland_service.dto.request;

import com.carland.carland_service.dto.response.v2.MoneyResponse;
import com.carland.carland_service.dto.response.v2.ServiceHistoryLineV2Response;
import com.carland.carland_service.dto.response.v2.ServiceHistoryPartV2Response;
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
public class PartnerUpdateServiceVisitRequest {
    private String vin;
    private Long partnerRecordId;
    private String type;
    private LocalDate date;
    private Integer mileage;
    private Long serviceCenterId;
    private String serviceCenterName;
    private String dealer;
    private MoneyResponse amount;
    private List<String> serviceGroups;
    private List<ServiceHistoryLineV2Response> services;
    private List<ServiceHistoryPartV2Response> parts;
}

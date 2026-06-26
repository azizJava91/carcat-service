package com.carland.carland_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarServicePercentageResponse {
    Long percentageId;
    Long serviceId;
    String serviceName;
    String serviceNameAz;
    String serviceNameEn;
    String serviceNameRu;
    String actionType;

    Long intervalKm;
    Integer intervalMonth;

    Integer kmPercentage;
    Integer monthPercentage;
    Integer monthPercentageDigit; // yeni faiz ile tarix faizini gosteren field elave etmisem
    Integer remainingKm;
    String  remainingMonths;

    Integer lastServiceKm;
    String lastServiceDate;

    Integer nextServiceKm;
    String nextServiceDate;
    String status;
    String servicedStatus;
    boolean important;
}

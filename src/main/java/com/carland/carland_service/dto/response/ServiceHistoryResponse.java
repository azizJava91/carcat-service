package com.carland.carland_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceHistoryResponse {

    Long id;
    String serviceName;
    String actionType;
    LocalDate doneDate;
    Integer doneKm;
    BigDecimal serviceAmount;
    Long workedAutoServiceId;
    String workedAutoServiceName;
}

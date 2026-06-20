package com.carland.carland_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceHistoryPartResponse {
    private String name;
    private BigDecimal qty;
    private String unit;
    private BigDecimal cost;
    private BigDecimal finalCost;
    private BigDecimal discount;
}

package com.carland.carland_service.dto.response.v2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoneyResponse {
    private BigDecimal amount;
    private String currency;
}

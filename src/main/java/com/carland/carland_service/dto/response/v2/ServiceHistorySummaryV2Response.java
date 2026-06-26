package com.carland.carland_service.dto.response.v2;

import com.carland.carland_service.dto.response.MoneyResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceHistorySummaryV2Response {
    private int serviceCount;
    private MoneyResponse totalAmount;
}

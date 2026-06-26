package com.carland.carland_service.dto.response.v2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarVinServiceHistoryV2Response {
    private String vin;
    private String source;
    private ServiceHistorySummaryV2Response summary;
    private List<ServiceHistoryVisitV2Response> items;
}

package com.carland.carland_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarVinServiceHistoryResponse {
    private String vin;
    private String source;
    private List<ServiceHistoryItemResponse> items;
}

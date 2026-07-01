package com.carland.carland_service.dto.response.v2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerNewServiceVisitResult {
    private String vin;
    private int visitsCreated;
    private int visitsSkipped;
    private int linesCreated;
    private int linesSkipped;
    private int partsCreated;
    private int partsSkipped;
}

package com.carland.carland_service.dto.response.v2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerUpdateServiceVisitResult {
    private String vin;
    private String message;
    private Long partnerRecordId;
    private Long visitId;
    private int linesUpdated;
    private int partsUpdated;
    @Builder.Default
    private List<LineUpdateDetail> lines = new ArrayList<>();
    @Builder.Default
    private List<PartUpdateDetail> parts = new ArrayList<>();
}

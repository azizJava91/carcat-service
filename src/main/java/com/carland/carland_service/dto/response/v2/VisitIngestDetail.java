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
public class VisitIngestDetail {
    private Long partnerRecordId;
    private Long visitId;
    private boolean visitCreated;
    @Builder.Default
    private List<LineIngestDetail> lines = new ArrayList<>();
}

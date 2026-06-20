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
public class MaintenanceTemplateResponse {
    Long id;
    String engineType;
    Long engineTypeId;
    String message;
    List<ServiceResponse> serviceResponseList;
}

package com.carland.carland_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MaintenanceTemplateRequest {
    String brand;
    String model;
    Integer year;
    String engineType;
    String transmissionType;
}

package com.carland.carland_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceRequest {

    String serviceName;

    String actionType;

    String nameAz;

    String nameEn;

    String nameRu;

    Long intervalKm;

    Integer intervalMonth;

}
package com.carland.carland_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceResponse {
    Long id;
    String serviceName;
    String actionType;
    Long intervalKm;
    Integer intervalMonth;
    Double amount;
    String nameAz;//    name_az
    String nameEn;//    name_en
    String nameRu;//    name_ru
}

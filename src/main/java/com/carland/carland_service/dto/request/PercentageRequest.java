package com.carland.carland_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PercentageRequest {
    Long carId;
    Long percentageId;
    LocalDate lastServiceDate;
    Integer lastServiceKm;
    LocalDate nextServiceDate;
    Integer nextServiceKm;
}

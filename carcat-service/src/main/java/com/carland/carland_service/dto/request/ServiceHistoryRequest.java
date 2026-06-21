package com.carland.carland_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceHistoryRequest {


    Long carId;
    String vin;
    String serviceName;
    LocalDate doneDate;
    Integer doneKm;
    BigDecimal serviceAmount;

}

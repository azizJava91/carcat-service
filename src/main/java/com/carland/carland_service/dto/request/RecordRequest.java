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
public class RecordRequest {
    Long carId;
    String vin;
    Long recordId;
    String serviceName;
    String actionType;
    LocalDate doneDate;
    Integer doneKm;
    String servicedStatus;
}

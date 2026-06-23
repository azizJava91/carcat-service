package com.carland.carland_service.dto.response;

import com.carland.carland_service.entity.Car;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordResponse {

    Long id;
    String serviceName;
    String serviceNameAz;
    String serviceNameRu;
    String actionType;
    LocalDate doneDate;
    Integer doneKm;
    String message;
    String servicedStatus;
}

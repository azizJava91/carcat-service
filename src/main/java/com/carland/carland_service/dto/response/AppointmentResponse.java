package com.carland.carland_service.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponse {
    Long id;
    String appointmentDate;
    String status;
    Long autoServiceId;
    String autoServiceName;
    String autoServiceNumber;
    String serviceCategory;
    String customerNumber;
    String customerName;
    String message;
    String appointmentStart;
    String appointmentEnd;
}

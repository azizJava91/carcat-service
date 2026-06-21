package com.carland.carland_service.dto.response;

import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {
    Long userId;
    String phoneNumber;
    String name;
    String surname;
    String status;

}

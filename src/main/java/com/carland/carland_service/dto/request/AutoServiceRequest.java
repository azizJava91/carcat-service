package com.carland.carland_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AutoServiceRequest {
    Long id;

    String name;
    String address;
    String phoneNumber;
    String email;

}

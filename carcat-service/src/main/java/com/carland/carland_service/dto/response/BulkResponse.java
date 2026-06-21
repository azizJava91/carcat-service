package com.carland.carland_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BulkResponse {
    String message;
    Integer totalItemCount;
    Integer successItemCount;
    Integer failedItemCount;
}

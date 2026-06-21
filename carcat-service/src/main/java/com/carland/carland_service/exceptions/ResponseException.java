package com.carland.carland_service.exceptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseException {
    private String error;
    private String message;
    private LocalDateTime timeStamp;
    private Integer status;
}

package com.carland.carland_service.exceptions;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ExpiredOtpException extends RuntimeException {
    public ExpiredOtpException(String message) {
        super(message);
    }
}

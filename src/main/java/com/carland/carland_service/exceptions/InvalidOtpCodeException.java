package com.carland.carland_service.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvalidOtpCodeException extends RuntimeException {
    public InvalidOtpCodeException(String message) {
        super(message);
    }
}

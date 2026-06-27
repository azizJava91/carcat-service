package com.carland.carland_service.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}

package com.carland.carland_service.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotMatchException extends RuntimeException {
    public NotMatchException(String message) {
        super(message);
    }
}

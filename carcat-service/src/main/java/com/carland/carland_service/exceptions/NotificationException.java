package com.carland.carland_service.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationException extends RuntimeException {
    public NotificationException(String message) {
        super(message);
    }
}

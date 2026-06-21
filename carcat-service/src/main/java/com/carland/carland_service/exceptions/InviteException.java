package com.carland.carland_service.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InviteException extends RuntimeException {
    public InviteException(String message) {
        super(message);
    }
}

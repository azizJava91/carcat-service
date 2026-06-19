package com.carland.carland_service.exceptions;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RefreshTokenNotSetException extends RuntimeException{

    public RefreshTokenNotSetException(String message){

        super(message);
    }
}

package com.carland.carland_service.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WrongPasswordException extends RuntimeException {

    public WrongPasswordException(String message){
        super(message);
    }

}

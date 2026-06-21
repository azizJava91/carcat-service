package com.carland.carland_service.exceptions;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UnderageException extends RuntimeException{

    public UnderageException(String message){

        super(message);
    }
}

package com.carland.carland_service.exceptions;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsernameAlreadyExistException extends RuntimeException{

    public UsernameAlreadyExistException(String message){
       super(message);
    }
}

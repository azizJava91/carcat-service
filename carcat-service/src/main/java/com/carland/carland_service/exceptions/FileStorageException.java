package com.carland.carland_service.exceptions;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FileStorageException extends RuntimeException {
    public FileStorageException(String message) {
        super(message);
    }
}

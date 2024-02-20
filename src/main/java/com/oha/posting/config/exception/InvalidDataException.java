package com.oha.posting.config.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class InvalidDataException extends RuntimeException{

    private final HttpStatus httpStatus;

    public InvalidDataException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }
}

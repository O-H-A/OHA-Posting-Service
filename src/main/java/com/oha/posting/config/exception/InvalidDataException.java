package com.oha.posting.config.exception;

public class InvalidDataException extends RuntimeException{
    private int statusCode;

    public InvalidDataException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}

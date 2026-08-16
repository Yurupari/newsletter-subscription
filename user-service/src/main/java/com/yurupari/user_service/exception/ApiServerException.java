package com.yurupari.user_service.exception;

public class ApiServerException extends RuntimeException {

    public ApiServerException(String message) {
        super(message);
    }
}

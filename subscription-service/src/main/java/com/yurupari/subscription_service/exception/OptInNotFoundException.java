package com.yurupari.subscription_service.exception;

public class OptInNotFoundException extends RuntimeException {

    public OptInNotFoundException(String message) {
        super(message);
    }
}

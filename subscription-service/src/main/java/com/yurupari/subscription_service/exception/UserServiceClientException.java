package com.yurupari.subscription_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public class UserServiceClientException extends RuntimeException {

    private final HttpStatus httpStatus;

    public UserServiceClientException(HttpStatusCode httpStatusCode, String identifier, String message) {
        this.httpStatus = HttpStatus.valueOf(httpStatusCode.value());

        super(String.format("User service error: identifier=%s, message=%s",
                identifier, message));
    }
}

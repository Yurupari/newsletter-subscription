package com.yurupari.user_service.exception;

public class KeycloakClientException extends RuntimeException {

    public KeycloakClientException(String email) {
        super(String.format("Authentication failed: email=%s", email));
    }
}

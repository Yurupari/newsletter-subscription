package com.yurupari.user_service.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super(String.format("User not found: id=%s", id));
    }

    public UserNotFoundException(String email) {
        super(String.format("User not found: email=%s", email));
    }
}

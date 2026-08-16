package com.yurupari.user_service.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String email) {
        super(String.format("User not found: email=%s", email));
    }

    public UserNotFoundException(Long id, String email) {
        super(String.format("User not found: id=%s, email=%s", id, email));
    }
}

package com.yurupari.user_service.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super(String.format("User not found: email=%s", id));
    }

    public UserNotFoundException(Long id, String email) {
        super(String.format("User not found: id=%s, email=%s", id, email));
    }
}

package com.yurupari.user_service.exception;

public class MultipleUsersException extends RuntimeException {

    public MultipleUsersException(Long id, String email) {
        super(String.format("Ambiguous request, provided ID and email matched different active user accounts: id=%s, email=%s",
                id, email));
    }
}

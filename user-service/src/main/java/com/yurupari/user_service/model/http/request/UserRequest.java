package com.yurupari.user_service.model.http.request;

import jakarta.validation.constraints.NotNull;

public record UserRequest(

        @NotNull(message = "Email is required")
        String email,

        @NotNull(message = "Password is required")
        String password,

        String firstName,
        String lastName
) {
}

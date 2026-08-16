package com.yurupari.user_service.model.http.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record LoginRequest(

        @NotNull(message = "Email is required")
        String email,

        @NotNull(message = "Password is required")
        String password
) {
}

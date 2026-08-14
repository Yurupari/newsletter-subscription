package com.yurupari.user_service.model.http.response;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName
) {
}

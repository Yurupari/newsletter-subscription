package com.yurupari.user_service.model.http.response;

import lombok.Builder;

@Builder
public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName
) {
}

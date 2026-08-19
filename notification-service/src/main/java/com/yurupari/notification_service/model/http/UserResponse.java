package com.yurupari.notification_service.model.http;

import lombok.Builder;

@Builder
public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName
) {
}
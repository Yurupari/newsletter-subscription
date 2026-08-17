package com.yurupari.user_service.kafka.event;

import lombok.Builder;

@Builder
public record RegisterUserEvent(
        Long userId,
        String email,
        String password,
        String firstName,
        String lastName
) {
}

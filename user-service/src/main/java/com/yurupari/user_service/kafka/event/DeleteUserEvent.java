package com.yurupari.user_service.kafka.event;

import lombok.Builder;

@Builder
public record DeleteUserEvent(
        Long userId,
        String keycloakUserId
) {
}

package com.yurupari.subscription_service.model.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record OptInDto(
        Long id,
        Long subscriptionId,
        String token,
        Instant expiresAt,
        Instant usedAt
) {
}

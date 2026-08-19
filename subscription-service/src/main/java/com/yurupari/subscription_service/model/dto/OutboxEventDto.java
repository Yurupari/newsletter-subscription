package com.yurupari.subscription_service.model.dto;

import com.yurupari.common_data.model.enums.OutboxStatus;
import com.yurupari.subscription_service.model.enums.OutboxAggregateType;
import com.yurupari.subscription_service.model.enums.OutboxEventType;
import lombok.Builder;

import java.time.Instant;

@Builder
public record OutboxEventDto(
        Long id,
        OutboxAggregateType aggregateType,
        Long aggregateId,
        OutboxEventType eventType,
        String payload,
        OutboxStatus status,
        Integer retryCount,
        Instant createdAt,
        Instant processedAt
) {
}

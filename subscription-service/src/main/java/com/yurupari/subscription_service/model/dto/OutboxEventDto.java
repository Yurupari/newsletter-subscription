package com.yurupari.subscription_service.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

@Builder
public record OutboxEventDto(
        Long id,
        String aggregateType,
        Long aggregateId,
        String eventType,
        String payload,
        String status,
        Integer retryCount,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") String createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") String processedAt
) {
}

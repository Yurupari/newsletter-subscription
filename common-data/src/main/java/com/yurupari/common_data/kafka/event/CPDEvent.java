package com.yurupari.common_data.kafka.event;

import lombok.Builder;

@Builder
public record CPDEvent(
        Long outboxId,
        String eventType,
        String source,
        Long aggregateId,
        String properties
) {
}

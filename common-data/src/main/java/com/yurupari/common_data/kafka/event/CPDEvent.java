package com.yurupari.common_data.kafka.event;

import lombok.Builder;

import java.util.Map;

@Builder
public record CPDEvent(
        Long outboxId,
        String eventType,
        String source,
        Long aggregateId,
        Map<String, String> properties
) {
}

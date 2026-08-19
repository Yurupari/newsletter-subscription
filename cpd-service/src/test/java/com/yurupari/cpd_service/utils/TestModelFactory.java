package com.yurupari.cpd_service.utils;

import com.yurupari.common_data.kafka.event.CPDEvent;
import com.yurupari.cpd_service.model.entity.OutboxEvent;
import com.yurupari.cpd_service.model.enums.OutboxAggregateType;
import com.yurupari.cpd_service.model.enums.OutboxEventType;

import java.time.Instant;
import java.util.Map;

public class TestModelFactory {

    public static CPDEvent buildCPDEvent(
            Long outboxId,
            String eventType,
            String source,
            Long aggregateId,
            Map<String, String> properties
    ) {
        return CPDEvent.builder()
                .outboxId(outboxId)
                .eventType(eventType)
                .source(source)
                .aggregateId(aggregateId)
                .properties(properties)
                .build();
    }

    public static OutboxEvent buildOutboxEvent(
            Long id,
            OutboxAggregateType aggregateType,
            Long aggregateId,
            OutboxEventType eventType,
            String payload,
            Integer retryCount,
            Instant createdAt,
            Instant processedAt
    ) {
        return OutboxEvent.builder()
                .id(id)
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(payload)
                .retryCount(retryCount)
                .createdAt(createdAt)
                .processedAt(processedAt)
                .build();
    }
}

package com.yurupari.subscription_service.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yurupari.subscription_service.model.dto.OutboxEventDto;
import com.yurupari.subscription_service.model.entity.OutboxEvent;
import com.yurupari.subscription_service.model.enums.OutboxEventType;
import com.yurupari.subscription_service.model.mapper.OutboxEventMapper;
import com.yurupari.subscription_service.repository.OutboxEventRepository;
import com.yurupari.subscription_service.service.OutboxEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventServiceImpl implements OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;

    private final OutboxEventMapper outboxEventMapper;

    private final ObjectMapper objectMapper;

    @Override
    public OutboxEventDto saveOutboxEvent(Long subscriptionId, OutboxEventType eventType, Map<String, String> payload) {
        log.info("Saving outbox event: subscriptionId={}, eventType={}",
                subscriptionId, eventType);

        var jsonPayload = objectMapper.writeValueAsString(payload);

        var outboxEvent = OutboxEvent.builder()
                .aggregateId(subscriptionId)
                .eventType(eventType)
                .payload(jsonPayload)
                .build();
        var savedOutboxEvent = outboxEventRepository.saveAndFlush(outboxEvent);

        return outboxEventMapper.toDto(savedOutboxEvent);
    }
}

package com.yurupari.subscription_service.service.impl;

import com.yurupari.subscription_service.model.dto.OutboxEventDto;
import com.yurupari.subscription_service.model.entity.OutboxEvent;
import com.yurupari.subscription_service.model.enums.OutboxEventType;
import com.yurupari.subscription_service.model.mapper.OutboxEventMapper;
import com.yurupari.subscription_service.repository.OutboxEventRepository;
import com.yurupari.subscription_service.service.OutboxEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventServiceImpl implements OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;

    private final OutboxEventMapper outboxEventMapper;

    @Override
    public OutboxEventDto saveOutboxEvent(Long subscriptionId, OutboxEventType eventType, String payload) {
        log.info("Saving outbox event: subscriptionId={}, eventType={}",
                subscriptionId, eventType);

        var outboxEvent = OutboxEvent.builder()
                .aggregateId(subscriptionId)
                .eventType(eventType)
                .payload(payload)
                .build();
        var savedOutboxEvent = outboxEventRepository.saveAndFlush(outboxEvent);

        return outboxEventMapper.toDto(savedOutboxEvent);
    }
}

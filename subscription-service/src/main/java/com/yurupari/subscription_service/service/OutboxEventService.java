package com.yurupari.subscription_service.service;

import com.yurupari.subscription_service.model.dto.OutboxEventDto;
import com.yurupari.subscription_service.model.enums.OutboxEventType;

import java.util.Map;

public interface OutboxEventService {

    OutboxEventDto saveOutboxEvent(Long subscriptionId, OutboxEventType eventType, Map<String, String> payload);
}

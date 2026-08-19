package com.yurupari.subscription_service.service;

import com.yurupari.subscription_service.model.dto.OutboxEventDto;
import com.yurupari.subscription_service.model.enums.OutboxEventType;

public interface OutboxEventService {

    OutboxEventDto saveOutboxEvent(Long subscriptionId, OutboxEventType eventType, String payload);
}

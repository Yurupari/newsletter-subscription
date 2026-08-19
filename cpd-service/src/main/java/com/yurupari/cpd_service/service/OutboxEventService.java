package com.yurupari.cpd_service.service;

public interface OutboxEventService {

    void updateOutboxEvent(Long id, Boolean isProcessed);
}

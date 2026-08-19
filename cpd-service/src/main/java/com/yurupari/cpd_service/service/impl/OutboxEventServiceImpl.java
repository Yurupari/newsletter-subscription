package com.yurupari.cpd_service.service.impl;

import com.yurupari.common_data.model.enums.OutboxStatus;
import com.yurupari.cpd_service.repository.OutboxEventRepository;
import com.yurupari.cpd_service.service.OutboxEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventServiceImpl implements OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;

    @Override
    public void updateOutboxEvent(Long id, Boolean isProcessed) {
        log.info("Updating outbox event: id={}, isProcessed={}", id, isProcessed);

        outboxEventRepository.findById(id).ifPresent(
                outboxEvent -> {
                    if (isProcessed) {
                        outboxEvent.setProcessedAt(Instant.now());
                        outboxEvent.setStatus(OutboxStatus.PROCESSED);
                    } else {
                        outboxEvent.setRetryCount(outboxEvent.getRetryCount() + 1);
                        outboxEvent.setStatus(OutboxStatus.FAILED);
                    }

                    outboxEventRepository.saveAndFlush(outboxEvent);
                }
        );
    }
}

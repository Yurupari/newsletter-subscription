package com.yurupari.cpd_service.service.impl;

import com.yurupari.common_data.kafka.event.CPDEvent;
import com.yurupari.cpd_service.client.CpdClient;
import com.yurupari.cpd_service.model.dto.IdentifyPayload;
import com.yurupari.cpd_service.model.dto.TrackPayload;
import com.yurupari.cpd_service.model.enums.OutboxEventType;
import com.yurupari.cpd_service.service.CPDService;
import com.yurupari.cpd_service.service.OutboxEventService;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CPDServiceImpl implements CPDService {

    private final OutboxEventService outboxEventService;

    private final CpdClient cpdClient;

    @Override
    @Retry(name = "cdp", fallbackMethod = "handleCdpFailure")
    public void sendCPDNotification(CPDEvent cpdEvent) {
        log.info("Sending notification...");

        String aggregateId = String.valueOf(cpdEvent.aggregateId());
        Map<String, Object> traitsAndProperties = new HashMap<>(cpdEvent.properties());

        switch (OutboxEventType.from(cpdEvent.eventType())) {
            case USER_CREATED, USER_UPDATED ->
                    cpdClient.identify(new IdentifyPayload(aggregateId, traitsAndProperties));

            case USER_DELETED ->
                    cpdClient.track(TrackPayload.builder()
                            .userId(aggregateId)
                            .event(OutboxEventType.USER_DELETED.name())
                            .properties(traitsAndProperties)
                            .build());

            default ->
                    cpdClient.track(TrackPayload.builder()
                            .userId(aggregateId)
                            .event(cpdEvent.eventType())
                            .properties(traitsAndProperties)
                            .build());
        }

        outboxEventService.updateOutboxEvent(cpdEvent.outboxId(), true);
    }

    public void handleCdpFailure(CPDEvent cpdEvent, Exception e) throws Exception {
        log.error("All retries exhausted for outboxId={}. Exception: {}", cpdEvent.outboxId(), e.getMessage());
        outboxEventService.updateOutboxEvent(cpdEvent.outboxId(), false);
        throw e;
    }
}

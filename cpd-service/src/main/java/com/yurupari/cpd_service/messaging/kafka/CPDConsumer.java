package com.yurupari.cpd_service.messaging.kafka;

import com.yurupari.common_data.kafka.event.CPDEvent;
import com.yurupari.cpd_service.service.CPDService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CPDConsumer {

    private final CPDService cpdService;

    @KafkaListener(topics = "cpd-notification", groupId = "cpd-service")
    public void consumeCPDEvent(CPDEvent cpdEvent) {
        log.info("Consumed CPD Notification Event: outboxId={}, eventType={}, source={}",
                cpdEvent.outboxId(), cpdEvent.eventType(), cpdEvent.source());
        log.info("EVENT={}", cpdEvent);
        cpdService.sendCPDNotification(cpdEvent);
    }
}

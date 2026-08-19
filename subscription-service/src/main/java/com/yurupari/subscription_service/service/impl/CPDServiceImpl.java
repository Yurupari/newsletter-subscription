package com.yurupari.subscription_service.service.impl;

import com.yurupari.common_data.kafka.event.CPDEvent;
import com.yurupari.subscription_service.messaging.kafka.SubscriptionProducer;
import com.yurupari.subscription_service.model.dto.NewsletterDto;
import com.yurupari.subscription_service.model.enums.ConsentType;
import com.yurupari.subscription_service.model.enums.OutboxEventType;
import com.yurupari.subscription_service.service.CPDService;
import com.yurupari.subscription_service.service.NewsletterService;
import com.yurupari.subscription_service.service.OutboxEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CPDServiceImpl implements CPDService {

    private final OutboxEventService outboxEventService;

    private final NewsletterService newsletterService;

    private final SubscriptionProducer subscriptionProducer;

    @Value("${spring.application.name}")
    private String source;

    @Override
    public void sendSubscriptionCPDNotification(Long userId, Long newsletterId, boolean isDoubleOptInConfirmed) {
        log.info("Preparing subscription CPD notification: userId={}, newsletterId={}",
                userId, newsletterId);

        newsletterService.getNewsletterById(newsletterId).ifPresent(
                newsletter -> sendEvent(userId, newsletter, OutboxEventType.NEWSLETTER_SUBSCRIBED)
        );
    }

    @Override
    public void sendUnsubscriptionCPDNotification(Long userId, Long newsletterId, boolean isDoubleOptInConfirmed) {
        log.info("Preparing unsubscription CPD notification: userId={}, newsletterId={}",
                userId, newsletterId);

        newsletterService.getNewsletterById(newsletterId).ifPresent(
                newsletter -> sendEvent(userId, newsletter, OutboxEventType.NEWSLETTER_UNSUBSCRIBED)
        );
    }

    private void sendEvent(Long userId, NewsletterDto newsletter, OutboxEventType outboxEventType) {
        Map<String, String> payload = new HashMap<>();
        payload.put("userId", String.valueOf(userId));
        payload.put("newsletterId", String.valueOf(newsletter.id()));
        payload.put("newsletterTitle", newsletter.title());
        payload.put("newsletterDescription", newsletter.description());
        payload.put("optInDoubleConfirmed", String.valueOf(OutboxEventType.NEWSLETTER_SUBSCRIBED.equals(outboxEventType)));
        payload.put("timestamp", Instant.now().toString());
        payload.put("source", source);

        var outboxEvent = outboxEventService.saveOutboxEvent(
                userId,
                outboxEventType,
                payload
        );

        var cpdEvent = CPDEvent.builder()
                .outboxId(outboxEvent.id())
                .eventType(outboxEventType.name())
                .source(source)
                .aggregateId(userId)
                .properties(payload)
                .build();

        subscriptionProducer.produceCPDEvent(cpdEvent);
    }
}

package com.yurupari.subscription_service.service.impl;

import com.yurupari.common_data.kafka.event.CPDEvent;
import com.yurupari.subscription_service.config.PropertiesPayloads;
import com.yurupari.subscription_service.messaging.kafka.SubscriptionProducer;
import com.yurupari.subscription_service.model.enums.ConsentType;
import com.yurupari.subscription_service.model.enums.OutboxEventType;
import com.yurupari.subscription_service.service.CPDService;
import com.yurupari.subscription_service.service.NewsletterService;
import com.yurupari.subscription_service.service.OutboxEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class CPDServiceImpl implements CPDService {

    private final OutboxEventService outboxEventService;

    private final NewsletterService newsletterService;

    private final SubscriptionProducer subscriptionProducer;

    private final PropertiesPayloads propertiesPayloads;

    @Value("${spring.application.name}")
    private String source;

    @Override
    public void sendSubscriptionCPDNotification(Long userId, Long newsletterId, boolean isDoubleOptInConfirmed) {
        log.info("Preparing subscription CPD notification: userId={}, newsletterId={}",
                userId, newsletterId);

        newsletterService.getNewsletterById(newsletterId).ifPresent(
                newsletter -> {
                    var template = getPropertiesTemplate(OutboxEventType.NEWSLETTER_SUBSCRIBED);
                    String properties = template.formatted(
                            newsletterId,
                            newsletter.title(),
                            isDoubleOptInConfirmed,
                            Instant.now(),
                            ConsentType.GRANTED
                    );

                    sendEvent(userId, properties, OutboxEventType.NEWSLETTER_SUBSCRIBED);
                }
        );
    }

    @Override
    public void sendUnsubscriptionCPDNotification(Long userId, Long newsletterId, boolean isDoubleOptInConfirmed) {
        log.info("Preparing unsubscription CPD notification: userId={}, newsletterId={}",
                userId, newsletterId);

        newsletterService.getNewsletterById(newsletterId).ifPresent(
                newsletter -> {
                    var template = getPropertiesTemplate(OutboxEventType.NEWSLETTER_UNSUBSCRIBED);
                    String properties = template.formatted(
                            newsletterId,
                            newsletter.title(),
                            isDoubleOptInConfirmed,
                            ConsentType.DENIED
                    );

                    sendEvent(userId, properties, OutboxEventType.NEWSLETTER_UNSUBSCRIBED);
                }
        );
    }

    private String getPropertiesTemplate(OutboxEventType outboxEventType) {
        var fileName = propertiesPayloads.files().get(outboxEventType.name());
        var filePath =propertiesPayloads.basePath() + fileName;

        try {
            log.info("Loading properties template: property={}", outboxEventType);

            var resource = new ClassPathResource(filePath);

            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendEvent(Long userId, String payload, OutboxEventType outboxEventType) {
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

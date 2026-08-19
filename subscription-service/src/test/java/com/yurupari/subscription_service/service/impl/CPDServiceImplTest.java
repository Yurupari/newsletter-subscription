package com.yurupari.subscription_service.service.impl;

import com.yurupari.common_data.model.enums.OutboxStatus;
import com.yurupari.subscription_service.messaging.kafka.SubscriptionProducer;
import com.yurupari.subscription_service.model.enums.OutboxAggregateType;
import com.yurupari.subscription_service.model.enums.OutboxEventType;
import com.yurupari.subscription_service.service.NewsletterService;
import com.yurupari.subscription_service.service.OutboxEventService;
import com.yurupari.subscription_service.utils.TestModelFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CPDServiceImplTest {

    @InjectMocks
    private CPDServiceImpl cpdService;

    @Mock
    private OutboxEventService outboxEventService;

    @Mock
    private NewsletterService newsletterService;

    @Mock
    private SubscriptionProducer subscriptionProducer;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cpdService, "source", "subscription-service");
    }

    @Test
    void sendSubscriptionCPDNotification_Success_Subscribed() {
        var newsletter = TestModelFactory.buildNewsletterDto(
                1L,
                "Newsletter",
                "Description",
                true
        );
        when(newsletterService.getNewsletterById(anyLong())).thenReturn(Optional.of(newsletter));

        var outboxEvent = TestModelFactory.buildOutboxEventDto(
                1L,
                OutboxAggregateType.SUBSCRIPTION,
                1L,
                OutboxEventType.NEWSLETTER_SUBSCRIBED,
                "payload",
                OutboxStatus.PENDING,
                0,
                Instant.now(),
                null
        );
        when(outboxEventService.saveOutboxEvent(anyLong(), any(), any())).thenReturn(outboxEvent);

        assertDoesNotThrow(() -> cpdService.sendSubscriptionCPDNotification(1L, 1L, true));

        verify(outboxEventService, times(1)).saveOutboxEvent(anyLong(), any(), any());
    }

    @Test
    void sendSubscriptionCPDNotification_Fail_NewsletterNotFound() {
        when(newsletterService.getNewsletterById(anyLong())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> cpdService.sendSubscriptionCPDNotification(1L, 1L, true));

        verify(outboxEventService, never()).saveOutboxEvent(anyLong(), any(), any());;
    }

    @Test
    void sendUnsubscriptionCPDNotification_Success() {
        var newsletter = TestModelFactory.buildNewsletterDto(
                1L,
                "Newsletter",
                "Description",
                true
        );
        when(newsletterService.getNewsletterById(anyLong())).thenReturn(Optional.of(newsletter));

        var outboxEvent = TestModelFactory.buildOutboxEventDto(
                1L,
                OutboxAggregateType.SUBSCRIPTION,
                1L,
                OutboxEventType.NEWSLETTER_UNSUBSCRIBED,
                "payload",
                OutboxStatus.PENDING,
                0,
                Instant.now(),
                null
        );
        when(outboxEventService.saveOutboxEvent(anyLong(), any(), any())).thenReturn(outboxEvent);

        assertDoesNotThrow(() -> cpdService.sendUnsubscriptionCPDNotification(1L, 1L, false));

        verify(outboxEventService, times(1)).saveOutboxEvent(anyLong(), any(), any());
    }

    @Test
    void sendUnsubscriptionCPDNotification_Fail_NewsletterNotFound() {
        when(newsletterService.getNewsletterById(anyLong())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> cpdService.sendUnsubscriptionCPDNotification(1L, 1L, false));

        verify(outboxEventService, never()).saveOutboxEvent(anyLong(), any(), any());
    }
}

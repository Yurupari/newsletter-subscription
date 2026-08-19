package com.yurupari.cpd_service.service.impl;

import com.yurupari.cpd_service.client.CPDClient;
import com.yurupari.cpd_service.service.OutboxEventService;
import com.yurupari.cpd_service.utils.TestModelFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CPDServiceImplTest {

    @InjectMocks
    private CPDServiceImpl cpdService;

    @Mock
    private OutboxEventService outboxEventService;

    @Mock
    private CPDClient cpdClient;

    @Test
    void sendCPDNotification_Success_NewsletterSubscribed() {
        var cpdEvent = TestModelFactory.buildCPDEvent(
                1L,
                "NEWSLETTER_SUBSCRIBED",
                "subscription-service",
                1L,
                Map.of("key", "value")
        );

        doNothing().when(cpdClient).track(any());
        doNothing().when(outboxEventService).updateOutboxEvent(anyLong(), anyBoolean());

        assertDoesNotThrow(() -> cpdService.sendCPDNotification(cpdEvent));

        verify(cpdClient, times((1))).track(any());
    }

    @Test
    void sendCPDNotification_Success_UserCreated() {
        var cpdEvent = TestModelFactory.buildCPDEvent(
                1L,
                "USER_CREATED",
                "user-service",
                1L,
                Map.of("key", "value")
        );

        doNothing().when(cpdClient).identify(any());
        doNothing().when(outboxEventService).updateOutboxEvent(anyLong(), anyBoolean());

        assertDoesNotThrow(() -> cpdService.sendCPDNotification(cpdEvent));

        verify(cpdClient, times((1))).identify(any());
    }

    @Test
    void sendCPDNotification_Success_UserUpdated() {
        var cpdEvent = TestModelFactory.buildCPDEvent(
                1L,
                "USER_UPDATED",
                "user-service",
                1L,
                Map.of("key", "value")
        );

        doNothing().when(cpdClient).identify(any());
        doNothing().when(outboxEventService).updateOutboxEvent(anyLong(), anyBoolean());

        assertDoesNotThrow(() -> cpdService.sendCPDNotification(cpdEvent));

        verify(cpdClient, times((1))).identify(any());
    }

    @Test
    void sendCPDNotification_Success_UserDeleted() {
        var cpdEvent = TestModelFactory.buildCPDEvent(
                1L,
                "USER_DELETED",
                "user-service",
                1L,
                Map.of("key", "value")
        );

        doNothing().when(cpdClient).track(any());
        doNothing().when(outboxEventService).updateOutboxEvent(anyLong(), anyBoolean());

        assertDoesNotThrow(() -> cpdService.sendCPDNotification(cpdEvent));

        verify(cpdClient, times((1))).track(any());
    }

    @Test
    void sendCPDNotification_Success_NewsletterUnsubscribed() {
        var cpdEvent = TestModelFactory.buildCPDEvent(
                1L,
                "NEWSLETTER_UNSUBSCRIBED",
                "subscription-service",
                1L,
                Map.of("key", "value")
        );

        doNothing().when(cpdClient).track(any());
        doNothing().when(outboxEventService).updateOutboxEvent(anyLong(), anyBoolean());

        assertDoesNotThrow(() -> cpdService.sendCPDNotification(cpdEvent));

        verify(cpdClient, times((1))).track(any());
    }
}
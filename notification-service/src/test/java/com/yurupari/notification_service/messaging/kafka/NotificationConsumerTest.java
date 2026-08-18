package com.yurupari.notification_service.messaging.kafka;

import com.yurupari.notification_service.service.UserService;
import com.yurupari.notification_service.utils.TestModelFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @InjectMocks
    private NotificationConsumer notificationConsumer;

    @Mock
    private UserService userService;

    @Test
    void consumeConfirmSubscriptionEvent() {
        var confirmSubscriptionEvent = TestModelFactory.buildConfirmSubscriptionEvent(
                1L,
                2L,
                3L,
                "token"
        );

        assertDoesNotThrow(() -> notificationConsumer.consumeConfirmSubscriptionEvent(confirmSubscriptionEvent));

        verify(userService, times(1)).sendConfirmationEmail(any());
    }
}
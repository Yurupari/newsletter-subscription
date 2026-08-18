package com.yurupari.notification_service.messaging.kafka;

import com.yurupari.common_data.kafka.event.ConfirmSubscriptionEvent;
import com.yurupari.notification_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final UserService userService;

    @KafkaListener(topics = "confirm-subscription", groupId = "notification-service")
    public void consumeConfirmSubscriptionEvent(ConfirmSubscriptionEvent confirmSubscriptionEvent) {
        log.info("Consumed Confirm Subscription Event: userId={}, subscriptionId={}",
                confirmSubscriptionEvent.userId(), confirmSubscriptionEvent.subscriptionId());
        userService.sendConfirmationEmail(confirmSubscriptionEvent);
    }

}

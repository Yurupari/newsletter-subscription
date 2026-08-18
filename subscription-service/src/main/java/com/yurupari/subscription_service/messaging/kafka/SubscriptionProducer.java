package com.yurupari.subscription_service.messaging.kafka;

import com.yurupari.common_data.kafka.event.ConfirmSubscriptionEvent;
import com.yurupari.common_data.kafka.event.UnsubscribeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topics.confirm-subscription}")
    private String confirmSubscriptionTopic;

    @Value("${spring.kafka.topics.unsubscribe}")
    private String unsubscribeTopic;

    public void produceConfirmSubscriptionEvent(ConfirmSubscriptionEvent confirmSubscriptionEvent) {
        log.info("Producing confirm subscription event: event={}", confirmSubscriptionEvent);

        kafkaTemplate.send(
                confirmSubscriptionTopic,
                confirmSubscriptionEvent.subscriptionId().toString(),
                confirmSubscriptionEvent)
                .whenComplete((result, ex) ->
                        Optional.ofNullable(ex).ifPresentOrElse(
                                e -> log.error("Unable to send Confirm Subscription Event: error={}",
                                        e.getMessage()),
                                () -> log.info("Sent Confirm Subscription Event: userId={}",
                                        confirmSubscriptionEvent.subscriptionId())
                        )
                );
    }

    public void produceUnsubscribeEvent(UnsubscribeEvent unsubscribeEvent) {
        log.info("Producing unsubscribe event: event={}", unsubscribeEvent);

        kafkaTemplate.send(
                unsubscribeTopic,
                unsubscribeEvent.subscriptionId().toString(),
                unsubscribeEvent)
                .whenComplete((result, ex) ->
                        Optional.ofNullable(ex).ifPresentOrElse(
                                e -> log.error("Unable to send Unsubscribe Event: error={}",
                                        e.getMessage()),
                                () -> log.info("Sent Unsubscribe Event: userId={}",
                                        unsubscribeEvent.subscriptionId())
                        )
                );;
    }
}

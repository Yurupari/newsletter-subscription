package com.yurupari.user_service.messaging.kafka;

import com.yurupari.common_data.kafka.event.CPDEvent;
import com.yurupari.user_service.kafka.event.DeleteUserEvent;
import com.yurupari.user_service.kafka.event.RegisterUserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topics.register-user}")
    private String registerUserTopic;

    @Value("${spring.kafka.topics.delete-user}")
    private String deleteUserTopic;

    @Value("${spring.kafka.topics.cpd}")
    private String cpdTopic;

    public void produceRegisterUserEvent(RegisterUserEvent registerUserEvent) {
        var key = String.valueOf(registerUserEvent.userId());
        log.info("Producing Register User Event: userId={}", key);

        kafkaTemplate.send(registerUserTopic, key, registerUserEvent)
                .whenComplete((result, ex) ->
                        Optional.ofNullable(ex).ifPresentOrElse(
                                e -> log.error("Unable to send Register User Event: error={}", e.getMessage()),
                                () -> log.info("Sent Register User Event: userId={}", registerUserEvent.userId())
                        )
                );
    }

    public void produceDeleteUserEvent(DeleteUserEvent deleteUserEvent) {
        var key = String.valueOf(deleteUserEvent.userId());
        log.info("Producing Delete User Event: userId={}", key);

        kafkaTemplate.send(deleteUserTopic, key, deleteUserEvent)
                .whenComplete((result, ex) ->
                        Optional.ofNullable(ex).ifPresentOrElse(
                                e -> log.error("Unable to send Delete User Event: error={}", e.getMessage()),
                                () -> log.info("Sent Delete User Event: userId={}", deleteUserEvent.userId())
                        )
                );
    }

    public void produceCPDEvent(CPDEvent cpdEvent) {
        log.info("Producing CPD notification: event={}", cpdEvent);

        kafkaTemplate.send(
                        cpdTopic,
                        cpdEvent.outboxId().toString(),
                        cpdEvent)
                .whenComplete((result, ex) ->
                        Optional.ofNullable(ex).ifPresentOrElse(
                                e -> log.error("Unable to send CPD notification: error={}",
                                        e.getMessage()),
                                () -> log.info("Sent CPD notification: userId={}",
                                        cpdEvent.eventType())
                        )
                );;
    }
}

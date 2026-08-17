package com.yurupari.user_service.messaging.kafka;

import com.yurupari.user_service.kafka.event.DeleteUserEvent;
import com.yurupari.user_service.kafka.event.RegisterUserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void produceRegisterUserEvent(RegisterUserEvent registerUserEvent) {
        var key = String.valueOf(registerUserEvent.userId());
        log.info("Producing Register User Event: userId={}", key);

        kafkaTemplate.send("register-user", key, registerUserEvent)
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

        kafkaTemplate.send("delete-user", key, deleteUserEvent)
                .whenComplete((result, ex) ->
                        Optional.ofNullable(ex).ifPresentOrElse(
                                e -> log.error("Unable to send Delete User Event: error={}", e.getMessage()),
                                () -> log.info("Sent Delete User Event: userId={}", deleteUserEvent.userId())
                        )
                );
    }
}

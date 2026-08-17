package com.yurupari.user_service.messaging.kafka;

import com.yurupari.user_service.kafka.event.DeleteUserEvent;
import com.yurupari.user_service.kafka.event.RegisterUserEvent;
import com.yurupari.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserConsumer {

    private final UserService userService;

    @KafkaListener(topics = "register-user", groupId = "user-service")
    public void consumeRegisterUserEvent(RegisterUserEvent registerUserEvent) {
        log.info("Consumed Register User Event: userId={}", registerUserEvent.userId());
        userService.activateUser(registerUserEvent);
    }

    @KafkaListener(topics = "delete-user", groupId = "user-service")
    public void consumeDeleteUserEvent(DeleteUserEvent deleteUserEvent) {
        log.info("Consumed Delete User Event: userId={}", deleteUserEvent.userId());
        userService.deactivateUser(deleteUserEvent);
    }
}

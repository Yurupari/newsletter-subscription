package com.yurupari.user_service.messaging.kafka;

import com.yurupari.user_service.kafka.event.DeleteUserEvent;
import com.yurupari.user_service.kafka.event.RegisterUserEvent;
import com.yurupari.user_service.service.UserService;
import com.yurupari.user_service.utils.TestModelFactory;
import org.junit.jupiter.api.BeforeEach;
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
class UserConsumerTest {

    @InjectMocks
    private UserConsumer userConsumer;

    @Mock
    private UserService userService;

    private RegisterUserEvent registerUserEvent;
    private DeleteUserEvent deleteUserEvent;

    @BeforeEach
    void setUp() {
        registerUserEvent = TestModelFactory.createRegisterUserEvent(
                1L,
                "test@email.com",
                "testPassword",
                "testName",
                "testLastName"
        );
        deleteUserEvent = TestModelFactory.createDeleteUserEvent(
                1L,
                "testKeycloakUserId"
        );
    }

    @Test
    void consumeRegisterUserEvent() {
        assertDoesNotThrow(() -> userConsumer.consumeRegisterUserEvent(registerUserEvent));

        verify(userService, times(1)).activateUser(any());
    }

    @Test
    void consumeDeleteUserEvent() {
        assertDoesNotThrow(() -> userConsumer.consumeDeleteUserEvent(deleteUserEvent));

        verify(userService, times(1)).deactivateUser(any());
    }
}
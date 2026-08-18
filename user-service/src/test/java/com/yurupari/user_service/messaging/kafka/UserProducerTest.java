package com.yurupari.user_service.messaging.kafka;

import com.yurupari.user_service.kafka.event.DeleteUserEvent;
import com.yurupari.user_service.kafka.event.RegisterUserEvent;
import com.yurupari.user_service.utils.TestModelFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProducerTest {

    @InjectMocks
    private UserProducer userProducer;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private RegisterUserEvent registerUserEvent;
    private DeleteUserEvent deleteUserEvent;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userProducer, "registerUserTopic", "register-user");
        ReflectionTestUtils.setField(userProducer, "deleteUserTopic", "delete-user");

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
    void produceRegisterUserEvent_Sent() {
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        assertDoesNotThrow(() -> userProducer.produceRegisterUserEvent(registerUserEvent));

        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any());
    }

    @Test
    void produceRegisterUserEvent_NotSent() {
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Kafka connection timeout")));

        assertDoesNotThrow(() -> userProducer.produceRegisterUserEvent(registerUserEvent));

        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any());
    }

    @Test
    void produceDeleteUserEvent_Sent() {
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        assertDoesNotThrow(() -> userProducer.produceDeleteUserEvent(deleteUserEvent));

        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any());
    }

    @Test
    void produceDeleteUserEvent_NotSent() {
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Kafka connection timeout")));

        assertDoesNotThrow(() -> userProducer.produceDeleteUserEvent(deleteUserEvent));

        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any());
    }
}
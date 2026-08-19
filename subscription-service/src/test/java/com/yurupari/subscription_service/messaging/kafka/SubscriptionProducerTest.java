package com.yurupari.subscription_service.messaging.kafka;

import com.yurupari.common_data.kafka.event.CPDEvent;
import com.yurupari.common_data.kafka.event.ConfirmSubscriptionEvent;
import com.yurupari.subscription_service.utils.TestModelFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionProducerTest {

    @InjectMocks
    private SubscriptionProducer subscriptionProducer;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private ConfirmSubscriptionEvent confirmSubscriptionEvent;
    private CPDEvent CPDEvent;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(subscriptionProducer, "confirmSubscriptionTopic", "confirm-subscription");
        ReflectionTestUtils.setField(subscriptionProducer, "cpdTopic", "cpd-notification");

        confirmSubscriptionEvent = TestModelFactory.buildRegisterUserEvent(
                1L,
                1L,
                1L
        );
        CPDEvent = TestModelFactory.buildCPDEvent(
                1L,
                "testEventType",
                "testSource",
                1L,
                Map.of("key", "value")
        );
    }

    @Test
    void produceConfirmSubscriptionEvent_Sent() {
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        assertDoesNotThrow(() -> subscriptionProducer.produceConfirmSubscriptionEvent(confirmSubscriptionEvent));

        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any());
    }

    @Test
    void produceConfirmSubscriptionEvent_NotSent() {
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Kafka connection timeout")));

        assertDoesNotThrow(() -> subscriptionProducer.produceConfirmSubscriptionEvent(confirmSubscriptionEvent));

        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any());
    }

    @Test
    void produceCPDEvent_Sent() {
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        assertDoesNotThrow(() -> subscriptionProducer.produceCPDEvent(CPDEvent));

        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any());
    }

    @Test
    void produceCPDEvent_NotSent() {
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Kafka connection timeout")));

        assertDoesNotThrow(() -> subscriptionProducer.produceCPDEvent(CPDEvent));

        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any());
    }
}

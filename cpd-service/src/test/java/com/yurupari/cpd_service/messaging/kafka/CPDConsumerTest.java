package com.yurupari.cpd_service.messaging.kafka;

import com.yurupari.cpd_service.service.CPDService;
import com.yurupari.cpd_service.utils.TestModelFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CPDConsumerTest {

    @InjectMocks
    private CPDConsumer cpdConsumer;

    @Mock
    private CPDService cpdService;

    @Test
    void consumeCPDEvent() {
        var cpdEvent = TestModelFactory.buildCPDEvent(
                1L,
                "NEWSLETTER_SUBSCRIBED",
                "subscription-service",
                1L,
                Map.of("key", "value")
        );

        assertDoesNotThrow(() -> cpdConsumer.consumeCPDEvent(cpdEvent));

        verify(cpdService, times(1)).sendCPDNotification(any());
    }
}
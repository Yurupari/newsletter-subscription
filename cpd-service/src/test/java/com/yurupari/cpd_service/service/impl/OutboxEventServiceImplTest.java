package com.yurupari.cpd_service.service.impl;

import com.yurupari.cpd_service.model.enums.OutboxAggregateType;
import com.yurupari.cpd_service.model.enums.OutboxEventType;
import com.yurupari.cpd_service.repository.OutboxEventRepository;
import com.yurupari.cpd_service.utils.TestModelFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxEventServiceImplTest {

    @InjectMocks
    private OutboxEventServiceImpl outboxEventService;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Test
    void updateOutboxEvent_Success_Processed() {
        var outboxEvent = TestModelFactory.buildOutboxEvent(
                1L,
                OutboxAggregateType.SUBSCRIPTION,
                1L,
                OutboxEventType.NEWSLETTER_SUBSCRIBED,
                "{}",
                0,
                Instant.now(),
                null
        );
        when(outboxEventRepository.findById(anyLong())).thenReturn(java.util.Optional.of(outboxEvent));

        assertDoesNotThrow(() -> outboxEventService.updateOutboxEvent(1L, true));

        verify(outboxEventRepository, times(1)).findById(anyLong());
        verify(outboxEventRepository, times(1)).saveAndFlush(any());
    }

    @Test
    void updateOutboxEvent_Success_NotProcessed() {
        var outboxEvent = TestModelFactory.buildOutboxEvent(
                1L,
                OutboxAggregateType.SUBSCRIPTION,
                1L,
                OutboxEventType.NEWSLETTER_SUBSCRIBED,
                "{}",
                0,
                Instant.now(),
                null
        );
        when(outboxEventRepository.findById(anyLong())).thenReturn(Optional.of(outboxEvent));

        assertDoesNotThrow(() -> outboxEventService.updateOutboxEvent(1L, false));

        verify(outboxEventRepository, times(1)).findById(anyLong());
        verify(outboxEventRepository, times(1)).saveAndFlush(any());
    }

    @Test
    void updateOutboxEvent_NotFound() {
        when(outboxEventRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> outboxEventService.updateOutboxEvent(1L, true));

        verify(outboxEventRepository, times(1)).findById(anyLong());
        verify(outboxEventRepository, never()).saveAndFlush(any());
    }
}
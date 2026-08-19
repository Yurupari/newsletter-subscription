package com.yurupari.subscription_service.service.impl;

import com.yurupari.common_data.model.enums.OutboxStatus;
import com.yurupari.subscription_service.model.enums.OutboxAggregateType;
import com.yurupari.subscription_service.model.enums.OutboxEventType;
import com.yurupari.subscription_service.model.mapper.OutboxEventMapperImpl;
import com.yurupari.subscription_service.repository.OutboxEventRepository;
import com.yurupari.subscription_service.utils.TestModelFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxEventServiceImplTest {

    @InjectMocks
    private OutboxEventServiceImpl outboxEventService;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Spy
    private OutboxEventMapperImpl outboxEventMapper = new OutboxEventMapperImpl();

    @Test
    void saveOutboxEvent() {
        var outboxEvent = TestModelFactory.buildOutboxEvent(
                1L,
                OutboxAggregateType.SUBSCRIPTION,
                1L,
                OutboxEventType.NEWSLETTER_SUBSCRIBED,
                "payload",
                OutboxStatus.PENDING,
                0,
                Instant.now(),
                null
        );
        when(outboxEventRepository.saveAndFlush(any())).thenReturn(outboxEvent);

        var response = outboxEventService.saveOutboxEvent(
                1L,
                OutboxEventType.NEWSLETTER_SUBSCRIBED,
                "payload"
        );

        assertNotNull(response);
    }
}
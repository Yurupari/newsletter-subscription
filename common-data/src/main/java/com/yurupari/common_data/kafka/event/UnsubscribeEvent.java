package com.yurupari.common_data.kafka.event;

import lombok.Builder;

@Builder
public record UnsubscribeEvent(
        Long subscriptionId,
        Long userId,
        Long newsletterId
) {
}

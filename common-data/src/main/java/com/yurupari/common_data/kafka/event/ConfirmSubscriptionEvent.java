package com.yurupari.common_data.kafka.event;

import lombok.Builder;

@Builder
public record ConfirmSubscriptionEvent(
        Long subscriptionId,
        Long userId,
        Long newsletterId,
        String token
) {
}

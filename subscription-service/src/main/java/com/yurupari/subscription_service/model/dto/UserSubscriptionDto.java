package com.yurupari.subscription_service.model.dto;

import com.yurupari.subscription_service.model.enums.SubscriptionStatus;
import lombok.Builder;

@Builder
public record UserSubscriptionDto(
        Long id,
        Long userId,
        Long newsletterId,
        SubscriptionStatus status
) {
}

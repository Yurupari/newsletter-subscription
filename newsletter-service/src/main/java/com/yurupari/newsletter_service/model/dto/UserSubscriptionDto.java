package com.yurupari.newsletter_service.model.dto;

import com.yurupari.newsletter_service.model.enums.SubscriptionStatus;
import lombok.Builder;

@Builder
public record UserSubscriptionDto(
        Long id,
        Long userId,
        Long newsletterId,
        SubscriptionStatus status
) {

}

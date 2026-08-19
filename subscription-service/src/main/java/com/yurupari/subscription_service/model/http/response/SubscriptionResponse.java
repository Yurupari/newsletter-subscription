package com.yurupari.subscription_service.model.http.response;

import com.yurupari.subscription_service.model.dto.NewsletterDto;
import com.yurupari.subscription_service.model.enums.SubscriptionStatus;
import lombok.Builder;

@Builder
public record SubscriptionResponse(
        Long id,
        Long userId,
        NewsletterDto newsletter,
        SubscriptionStatus status
) {
}

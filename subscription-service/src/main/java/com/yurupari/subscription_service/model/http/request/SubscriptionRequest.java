package com.yurupari.subscription_service.model.http.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record SubscriptionRequest(
        @NotNull(message = "Newsletter id must be provided")
        Long newsletterId
) {
}

package com.yurupari.subscription_service.model.http.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record NewsletterRequest(
        @NotNull(message = "Title must be provided")
        String title,

        String description
) {
}

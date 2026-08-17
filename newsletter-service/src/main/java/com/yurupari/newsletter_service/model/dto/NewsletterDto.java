package com.yurupari.newsletter_service.model.dto;

import lombok.Builder;

@Builder
public record NewsletterDto(
        Long id,
        String title,
        String description,
        Boolean isActive
) {
}

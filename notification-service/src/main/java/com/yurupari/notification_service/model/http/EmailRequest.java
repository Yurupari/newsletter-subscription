package com.yurupari.notification_service.model.http;

import lombok.Builder;

@Builder
public record EmailRequest(
        String to,
        String subject,
        String body
) {}

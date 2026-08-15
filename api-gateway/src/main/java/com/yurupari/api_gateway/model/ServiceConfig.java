package com.yurupari.api_gateway.model;

public record ServiceConfig(
        String url,
        String path,
        String circuitBreakerId,
        String fallbackPath,
        String fallbackMessage
) {
}

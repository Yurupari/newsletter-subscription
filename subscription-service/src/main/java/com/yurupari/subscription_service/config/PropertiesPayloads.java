package com.yurupari.subscription_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "templates")
public record PropertiesPayloads(
        String basePath,
        Map<String, String> files
) {
}

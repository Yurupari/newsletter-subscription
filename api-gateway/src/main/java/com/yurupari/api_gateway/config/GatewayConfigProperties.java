package com.yurupari.api_gateway.config;

import com.yurupari.api_gateway.model.ServiceConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "services")
public record GatewayConfigProperties(
        Map<String, ServiceConfig> items
) {
}

package com.yurupari.api_gateway.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GatewayService {

    USER("user-service"),
    DEVICE("device-service"),
    INGESTION("ingestion-service"),
    INSIGHT("insight-service");

    private final String name;
}

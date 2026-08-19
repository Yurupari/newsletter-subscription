package com.yurupari.cpd_service.model.dto;

import lombok.Builder;

import java.util.Map;

@Builder
public record IdentifyPayload(
        String userId,
        Map<String, Object> traits
) {
}

package com.yurupari.cpd_service.model.dto;

import lombok.Builder;

import java.util.Map;

@Builder
public record TrackPayload(
        String userId,
        String event,
        Map<String, Object> properties
) {
}

package com.yurupari.user_service.model.dto;

import lombok.Builder;

@Builder
public record CredentialDto(
        String type,
        String value,
        Boolean temporary
) {
}

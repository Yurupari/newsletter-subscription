package com.yurupari.user_service.model.http.request;

import com.yurupari.user_service.model.dto.CredentialDto;
import lombok.Builder;

import java.util.List;

@Builder
public record KeycloakUserRepresentationRequest(
        String username,
        String email,
        Boolean enabled,
        Boolean emailVerified,
        String firstName,
        String lastName,
        List<CredentialDto> credentials
) {
}

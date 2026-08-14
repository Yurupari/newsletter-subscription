package com.yurupari.user_service.model.http.response;

import lombok.Builder;

@Builder
public record AuthenticationResponse(
        String accessToken,
        String tokenType,
        Long expiresIn
) {
}

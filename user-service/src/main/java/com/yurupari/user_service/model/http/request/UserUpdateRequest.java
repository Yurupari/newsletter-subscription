package com.yurupari.user_service.model.http.request;

import lombok.Builder;

@Builder
public record UserUpdateRequest(
        String password,
        String firstName,
        String lastName
) {
}

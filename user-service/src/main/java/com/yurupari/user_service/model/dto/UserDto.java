package com.yurupari.user_service.model.dto;

import lombok.Builder;

@Builder
public record UserDto(
        Long id,
        String email,
        String password,
        String firstName,
        String lastName
) {
}

package com.yurupari.user_service.model.dto;

public record UserDto(
        Long id,
        String email,
        String firstName,
        String lastName
) {
}

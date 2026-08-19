package com.yurupari.user_service.utils;

import com.yurupari.user_service.kafka.event.DeleteUserEvent;
import com.yurupari.user_service.kafka.event.RegisterUserEvent;
import com.yurupari.user_service.model.dto.UserDto;
import com.yurupari.user_service.model.entity.User;
import com.yurupari.user_service.model.enums.UserStatus;
import com.yurupari.user_service.model.http.request.LoginRequest;
import com.yurupari.user_service.model.http.request.UserRequest;
import com.yurupari.user_service.model.http.request.UserUpdateRequest;
import com.yurupari.user_service.model.http.response.AuthenticationResponse;
import com.yurupari.user_service.model.http.response.UserResponse;

import java.time.Instant;

public class TestModelFactory {

    public static UserRequest createUserRequest(
            String email,
            String password,
            String firstName,
            String lastName
    ) {
        return UserRequest.builder()
                .email(email)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }

    public static UserResponse createUserResponse(
            Long id,
            String email,
            String firstName,
            String lastName
    ) {
        return UserResponse.builder()
                .id(id)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }

    public static User createUser(
            Long id,
            String email,
            String password,
            String firstName,
            String lastName,
            UserStatus status,
            String authUserId,
            Long version,
            Instant createdAt,
            Instant updatedAt
    ) {
        return User.builder()
                .id(id)
                .email(email)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .status(status)
                .authUserId(authUserId)
                .version(version)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static UserUpdateRequest createUserUpdateRequest(
            String password,
            String firstName,
            String lastName
    ) {
        return UserUpdateRequest.builder()
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }

    public static UserDto createUserDto(
            String email,
            String password,
            String firstName,
            String lastName
    ) {
        return UserDto.builder()
                .email(email)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }

    public static LoginRequest createLoginRequest(
            String email,
            String password
    ) {
        return LoginRequest.builder()
                .email(email)
                .password(password)
                .build();
    }

    public static AuthenticationResponse createAuthenticationResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            Long expiresIn
    ) {
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(tokenType)
                .expiresIn(expiresIn)
                .build();
    }

    public static RegisterUserEvent createRegisterUserEvent(
            Long userId,
            String email,
            String password,
            String firstName,
            String lastName
    ) {
        return RegisterUserEvent.builder()
                .userId(userId)
                .email(email)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }

    public static DeleteUserEvent createDeleteUserEvent(
            Long userId,
            String keycloakUserId
    ) {
        return DeleteUserEvent.builder()
                .userId(userId)
                .keycloakUserId(keycloakUserId)
                .build();
    }
}

package com.yurupari.user_service.service.impl;

import com.yurupari.common_data.kafka.event.RegisterUserEvent;
import com.yurupari.user_service.client.KeycloakClient;
import com.yurupari.user_service.model.dto.CredentialDto;
import com.yurupari.user_service.model.http.request.KeycloakUserRepresentationRequest;
import com.yurupari.user_service.model.http.request.LoginRequest;
import com.yurupari.user_service.model.http.response.AuthenticationResponse;
import com.yurupari.user_service.service.AuthenticationService;
import com.yurupari.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final KeycloakClient keycloakClient;

    private final UserService userService;

    @Value("${keycloak.realm}")
    private String realm;

    @Override
    public void createUser(RegisterUserEvent registerUserEvent) {
        var adminToken = "Bearer " + getAdminAccessToken();

        var credential = CredentialDto.builder()
                .type("password")
                .value(registerUserEvent.password())
                .temporary(false)
                .build();
        var keycloakUser = KeycloakUserRepresentationRequest.builder()
                .username(registerUserEvent.email())
                .email(registerUserEvent.email())
                .enabled(true)
                .emailVerified(true)
                .firstName(registerUserEvent.firstName())
                .lastName(registerUserEvent.lastName())
                .credentials(List.of(credential))
                .build();

        keycloakClient.createUser(realm, adminToken, keycloakUser);

        userService.activateUser(registerUserEvent.userId());
    }

    @Override
    public AuthenticationResponse authenticate(LoginRequest loginRequest) {
        log.info("Authenticating user: email={}", loginRequest.email());

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("username", loginRequest.email());
        formData.add("password", loginRequest.password());

        return keycloakClient.authenticate(realm, formData);
    }

    private String getAdminAccessToken() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");

        AuthenticationResponse response = keycloakClient.authenticate(realm, formData);
        return response.accessToken();
    }
}

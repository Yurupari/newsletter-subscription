package com.yurupari.user_service.service.impl;

import com.yurupari.user_service.kafka.event.DeleteUserEvent;
import com.yurupari.user_service.kafka.event.RegisterUserEvent;
import com.yurupari.user_service.client.KeycloakClient;
import com.yurupari.user_service.model.dto.CredentialDto;
import com.yurupari.user_service.model.http.request.KeycloakUserRepresentationRequest;
import com.yurupari.user_service.model.http.request.LoginRequest;
import com.yurupari.user_service.model.http.response.AuthenticationResponse;
import com.yurupari.user_service.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final KeycloakClient keycloakClient;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Override
    public String createUser(RegisterUserEvent registerUserEvent) {
        log.info("Create user in Keycloak: userId={}, email={}",
                registerUserEvent.userId(), registerUserEvent.email());

        try {
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

            log.info("Attempting to create user in Keycloak: userId={}, email={}",
                    registerUserEvent.userId(), registerUserEvent.email());
            var keycloakResponse = keycloakClient.createUser(realm, adminToken, keycloakUser);

            return Optional.ofNullable(keycloakResponse.getHeaders().getLocation())
                    .map(uri -> {
                        var path = uri.getPath();

                        return path.substring(path.lastIndexOf('/') + 1);
                    })
                    .orElseGet(() -> {
                        log.error("Keycloak User ID could not be created: userId={}", registerUserEvent.userId());
                        return null;
                    });
        } catch (Exception e) {
            log.error("Error creating user in Keycloak: userId={}, error={}",
                    registerUserEvent.userId(), e.getMessage());
            return null;
        }
    }

    @Override
    public AuthenticationResponse authenticate(LoginRequest loginRequest) {
        log.info("Authenticating user: email={}", loginRequest.email());

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("username", loginRequest.email());
        formData.add("password", loginRequest.password());

        return keycloakClient.authenticateUser(realm, formData);
    }

    @Override
    public void deleteUser(DeleteUserEvent deleteUserEvent) {
        log.info("Deleting user in Keycloak: userId={}", deleteUserEvent.userId());

        try {
            var adminToken = "Bearer " + getAdminAccessToken();

            keycloakClient.deleteUser(realm, deleteUserEvent.keycloakUserId(), adminToken);
        } catch(Exception e) {
            log.error("Error deleting user in Keycloak: userId={}, error={}",
                    deleteUserEvent.userId(), e.getMessage());
        }
    }

    private String getAdminAccessToken() {
        log.info("Attempting to get admin access token...");

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");

        var basicAuthHeader = "Basic " + Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));

        return Optional.ofNullable(keycloakClient.authenticateClient(realm, basicAuthHeader, formData))
                .map(AuthenticationResponse::accessToken)
                .orElse(null);
    }
}

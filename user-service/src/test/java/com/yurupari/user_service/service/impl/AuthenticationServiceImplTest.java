package com.yurupari.user_service.service.impl;

import com.yurupari.user_service.client.KeycloakClient;
import com.yurupari.user_service.kafka.event.DeleteUserEvent;
import com.yurupari.user_service.kafka.event.RegisterUserEvent;
import com.yurupari.user_service.model.http.response.AuthenticationResponse;
import com.yurupari.user_service.utils.TestModelFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @InjectMocks
    private AuthenticationServiceImpl authService;

    @Mock
    private KeycloakClient keycloakClient;

    private RegisterUserEvent registerUserEvent;
    private DeleteUserEvent deleteUserEvent;
    private AuthenticationResponse authenticationResponse;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "realm", "realm");
        ReflectionTestUtils.setField(authService, "credential", "credential");

        registerUserEvent = TestModelFactory.createRegisterUserEvent(
                1L,
                "test@email.com",
                "testPassword",
                "testName",
                "testLastName"
        );
        deleteUserEvent = TestModelFactory.createDeleteUserEvent(
                1L,
                "keycloakUserId"
        );

        authenticationResponse = TestModelFactory.createAuthenticationResponse(
                "accessToken",
                "refreshToken",
                "Bearer",
                3600L
        );
    }

    @Test
    void createUser_Success() {
        when(keycloakClient.authenticateClient(anyString(), anyString(), any())).thenReturn(authenticationResponse);

        var locationUri = URI.create("http://localhost:8091/admin/realms/ns-security-realm/users/123456");
        ResponseEntity<Void> keycloakResponse = ResponseEntity.created(locationUri).build();
        when(keycloakClient.createUser(anyString(), anyString(), any())).thenReturn(keycloakResponse);

        var keycloakUserId = authService.createUser(registerUserEvent);

        assertNotNull(keycloakUserId);
        assertEquals("123456", keycloakUserId);
    }

    @Test
    void createUser_Fail_WithoutHeaderLocation() {
        when(keycloakClient.authenticateClient(anyString(), anyString(), any())).thenReturn(authenticationResponse);

        ResponseEntity<Void> keycloakResponse = ResponseEntity.ok().build();
        when(keycloakClient.createUser(anyString(), anyString(), any())).thenReturn(keycloakResponse);

        var keycloakUserId = authService.createUser(registerUserEvent);

        assertNull(keycloakUserId);
    }

    @Test
    void createUser_Fail_GetAdminAccessTokenReturnsNull() {
        when(keycloakClient.authenticateClient(anyString(), anyString(), any())).thenReturn(null);
        when(keycloakClient.createUser(anyString(), anyString(), any())).thenThrow(new RuntimeException("Keycloak error"));

        var response = authService.createUser(registerUserEvent);

        verify(keycloakClient, times(1)).authenticateClient(anyString(), anyString(), any());
        verify(keycloakClient, times(1)).createUser(anyString(), anyString(), any());

        assertNull(response);
    }

    @Test
    void createUser_Fail_KeycloakClientThrowsException() {
        when(keycloakClient.authenticateClient(anyString(), anyString(), any())).thenThrow(new RuntimeException("Keycloak error"));

        var keycloakUserId = authService.createUser(registerUserEvent);

        assertNull(keycloakUserId);
        verify(keycloakClient, never()).createUser(anyString(), anyString(), any());
    }

    @Test
    void authenticate_Success() {
        when(keycloakClient.authenticateUser(anyString(), anyString(), any())).thenReturn(authenticationResponse);

        var response = authService.authenticate("test@email.com", "testPassword");

        assertNotNull(response);
        assertEquals("accessToken", response.accessToken());
    }

    @Test
    void authenticate_KeycloakClientThrowsException_ShouldPropagate() {
        when(keycloakClient.authenticateUser(anyString(), anyString(), any())).thenThrow(new RuntimeException("Keycloak error"));

        assertThrows(RuntimeException.class, () -> {
            authService.authenticate("test@email.com", "wrongPassword");
        });
    }

    @Test
    void deleteUser_Success() {
        when(keycloakClient.authenticateClient(anyString(), anyString(), any())).thenReturn(authenticationResponse);

        assertDoesNotThrow(() -> authService.deleteUser(deleteUserEvent));

        verify(keycloakClient, times(1)).deleteUser(anyString(), anyString(), anyString());
    }

    @Test
    void deleteUser_Fail_KeycloakClientThrowsException() {
        when(keycloakClient.authenticateClient(anyString(), anyString(), any())).thenThrow(new RuntimeException("Keycloak error"));

        assertDoesNotThrow(() -> authService.deleteUser(deleteUserEvent));

        verify(keycloakClient, never()).deleteUser(anyString(), anyString(), anyString());
    }
}

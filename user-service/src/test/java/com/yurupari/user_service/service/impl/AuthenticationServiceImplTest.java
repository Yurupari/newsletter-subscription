package com.yurupari.user_service.service.impl;

import com.yurupari.user_service.client.KeycloakClient;
import com.yurupari.user_service.utils.TestModelFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @InjectMocks
    private AuthenticationServiceImpl authService;

    @Mock
    private KeycloakClient keycloakClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "clientId", "client-id");
        ReflectionTestUtils.setField(authService, "clientSecret", "client-secret");
    }

    @Test
    void authenticate_Success() {
        var loginRequest = TestModelFactory.createLoginRequest(
                "test@email.com",
                "testPassword"
        );

        var authenticationResponse = TestModelFactory.createAuthenticationResponse(
                "accessToken",
                "refreshToken",
                "Bearer",
                3600L
        );
        when(keycloakClient.authenticate(any())).thenReturn(authenticationResponse);

        var response = authService.authenticate(loginRequest);

        assertNotNull(response);
        assertEquals("accessToken", response.accessToken());
        assertEquals("refreshToken", response.refreshToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600L, response.expiresIn());
    }

    @Test
    void authenticate_KeycloakClientThrowsException_ShouldPropagate() {
        var loginRequest = TestModelFactory.createLoginRequest(
                "test@email.com",
                "wrongPassword"
        );

        when(keycloakClient.authenticate(any())).thenThrow(new RuntimeException("Keycloak error"));

        assertThrows(RuntimeException.class, () -> {
            authService.authenticate(loginRequest);
        });
    }
}
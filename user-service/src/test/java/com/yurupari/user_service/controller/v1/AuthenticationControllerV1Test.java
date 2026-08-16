package com.yurupari.user_service.controller.v1;

import com.yurupari.user_service.model.http.request.LoginRequest;
import com.yurupari.user_service.model.http.response.AuthenticationResponse;
import com.yurupari.user_service.service.UserService;
import com.yurupari.user_service.utils.TestModelFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerV1Test {

    @InjectMocks
    private AuthenticationControllerV1 authenticationControllerV1;

    @Mock
    private UserService userService;

    private LoginRequest loginRequest;
    private AuthenticationResponse authResponse;

    @BeforeEach
    void setUp() {
        loginRequest = TestModelFactory.createLoginRequest(
                "test@email.com",
                "testPassword"
        );
        authResponse = TestModelFactory.createAuthenticationResponse(
                "accessToken",
                "refreshToken",
                "Bearer",
                3600L
        );
    }

    @Test
    void login_Success() {
        when(userService.login(any(LoginRequest.class))).thenReturn(authResponse);

        ResponseEntity<AuthenticationResponse> response = authenticationControllerV1.login(loginRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(authResponse, response.getBody());
    }

    @Test
    void login_Fail_BadCredentials() {
        when(userService.login(any(LoginRequest.class))).thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () -> authenticationControllerV1.login(loginRequest));
    }
}

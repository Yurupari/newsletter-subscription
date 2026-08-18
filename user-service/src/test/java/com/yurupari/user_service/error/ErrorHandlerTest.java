package com.yurupari.user_service.error;

import com.yurupari.common_data.model.http.ErrorResponse;
import com.yurupari.user_service.exception.AuthenticationException;
import com.yurupari.user_service.exception.KeycloakClientException;
import com.yurupari.user_service.exception.UserAlreadyExistsException;
import com.yurupari.user_service.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class ErrorHandlerTest {

    @InjectMocks
    private ErrorHandler errorHandler;

    @Test
    void handleUserNotFoundException() {
        var exception = new UserNotFoundException(1L, "test@example.com");
        ResponseEntity<ErrorResponse> responseEntity = errorHandler.handleUserNotFoundException(exception);
        assertErrorResponse(responseEntity, HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @Test
    void handleUserAlreadyExistsException() {
        var exception = new UserAlreadyExistsException("test@example.com");
        ResponseEntity<ErrorResponse> responseEntity = errorHandler.handleUserAlreadyExistsException(exception);
        assertErrorResponse(responseEntity, HttpStatus.CONFLICT, exception.getMessage());
    }

    @Test
    void handleIllegalArgumentException() {
        var exception = new IllegalArgumentException("Illegal argument");
        ResponseEntity<ErrorResponse> responseEntity = errorHandler.handleIllegalArgumentException(exception);
        assertErrorResponse(responseEntity, HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @Test
    void handleAuthenticationException() {
        var exception = new AuthenticationException("Bad credentials");
        ResponseEntity<ErrorResponse> responseEntity = errorHandler.handleAuthenticationException(exception);
        assertErrorResponse(responseEntity, HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @Test
    void handleKeycloakClientException() {
        var exception = new KeycloakClientException("Keycloak error");
        ResponseEntity<ErrorResponse> responseEntity = errorHandler.handleKeycloakClientException(exception);
        assertErrorResponse(responseEntity, HttpStatus.FORBIDDEN, exception.getMessage());
    }

    private void assertErrorResponse(
            ResponseEntity<ErrorResponse> responseEntity,
            HttpStatus expectedStatus,
            String expectedMessage
    ) {
        assertNotNull(responseEntity);
        assertEquals(expectedStatus, responseEntity.getStatusCode());

        ErrorResponse errorResponse = responseEntity.getBody();
        assertNotNull(errorResponse);
        assertEquals(expectedStatus, errorResponse.httpStatus());
        assertEquals(expectedMessage, errorResponse.message());
        assertNotNull(errorResponse.timestamp());
    }
}

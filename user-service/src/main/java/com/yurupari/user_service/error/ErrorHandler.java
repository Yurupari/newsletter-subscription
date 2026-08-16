package com.yurupari.user_service.error;

import com.yurupari.common_data.model.http.ErrorResponse;
import com.yurupari.user_service.exception.KeycloakClientException;
import com.yurupari.user_service.exception.UserAlreadyExistsException;
import com.yurupari.user_service.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.time.OffsetDateTime;

@ControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException e) {
        var errorResponse = buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.httpStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        var errorResponse = buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.httpStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        var errorResponse = buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.httpStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException e) {
        var errorResponse = buildErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.httpStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleKeycloakClientException(KeycloakClientException e) {
        var errorResponse = buildErrorResponse(HttpStatus.FORBIDDEN, e.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.httpStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        var errorResponse = buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.httpStatus());
    }

    private ErrorResponse buildErrorResponse(HttpStatus httpStatus, String message) {
        log.error(message);

        return ErrorResponse.builder()
                .httpStatus(httpStatus)
                .timestamp(OffsetDateTime.now())
                .message(message)
                .build();
    }
}

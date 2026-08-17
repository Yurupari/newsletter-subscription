package com.yurupari.user_service.error;

import com.yurupari.common_data.error.BaseErrorHandler;
import com.yurupari.common_data.model.http.ErrorResponse;
import com.yurupari.user_service.exception.AuthenticationException;
import com.yurupari.user_service.exception.KeycloakClientException;
import com.yurupari.user_service.exception.UserAlreadyExistsException;
import com.yurupari.user_service.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class ErrorHandler extends BaseErrorHandler {

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
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e) {
        var errorResponse = buildErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.httpStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleKeycloakClientException(KeycloakClientException e) {
        var errorResponse = buildErrorResponse(HttpStatus.FORBIDDEN, e.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.httpStatus());
    }
}

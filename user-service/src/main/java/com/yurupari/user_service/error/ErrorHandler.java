package com.yurupari.user_service.error;

import com.yurupari.common_data.model.http.ErrorResponse;
import com.yurupari.user_service.exception.UserAlreadyExistsException;
import com.yurupari.user_service.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

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
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        var errorResponse = buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.httpStatus());
    }

    private ErrorResponse buildErrorResponse(HttpStatus httpStatus, String message) {
        log.error(message);

        return ErrorResponse.builder()
                .httpStatus(httpStatus)
                .timestamp(Instant.now())
                .message(message)
                .build();
    }
}

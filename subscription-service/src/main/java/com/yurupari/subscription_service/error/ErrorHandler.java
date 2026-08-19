package com.yurupari.subscription_service.error;

import com.yurupari.common_data.error.BaseErrorHandler;
import com.yurupari.common_data.exception.ApiServerException;
import com.yurupari.common_data.model.http.ErrorResponse;
import com.yurupari.subscription_service.exception.NewsletterNotFoundException;
import com.yurupari.subscription_service.exception.OptInNotFoundException;
import com.yurupari.subscription_service.exception.UserServiceClientException;
import com.yurupari.subscription_service.exception.UserSubscriptionAlreadyUnsubscribedException;
import com.yurupari.subscription_service.exception.UserSubscriptionExistsException;
import com.yurupari.subscription_service.exception.UserSubscriptionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ErrorHandler extends BaseErrorHandler {

    @ExceptionHandler(UserServiceClientException.class)
    public ResponseEntity<ErrorResponse> handleUserServiceClientException(UserServiceClientException e) {
        var errorResponse = buildErrorResponse(e.getHttpStatus(), e.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.httpStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleNewsletterNotFoundException(NewsletterNotFoundException e) {
        var errorResponse = buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.httpStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleUserSubscriptionExistsException(UserSubscriptionExistsException e) {
        var errorResponse = buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.httpStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleUserSubscriptionAlreadyUnsubscribedException(UserSubscriptionAlreadyUnsubscribedException e) {
        var errorResponse = buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.httpStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleOptInNotFoundException(OptInNotFoundException e) {
        var errorResponse = buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.httpStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleUserSubscriptionNotFoundException(UserSubscriptionNotFoundException e) {
        var errorResponse = buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.httpStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleApiServerException(ApiServerException e) {
        var errorResponse = buildErrorResponse(HttpStatus.BAD_GATEWAY, e.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.httpStatus());
    }
}

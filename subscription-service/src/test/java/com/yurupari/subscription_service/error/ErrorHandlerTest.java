package com.yurupari.subscription_service.error;

import com.yurupari.subscription_service.exception.NewsletterNotFoundException;
import com.yurupari.subscription_service.exception.UserServiceClientException;
import com.yurupari.subscription_service.exception.UserSubscriptionNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class ErrorHandlerTest {

    @InjectMocks
    private ErrorHandler errorHandler;

    @Test
    void handleUserServiceClientException() {
        var exception = new UserServiceClientException(HttpStatusCode.valueOf(404), "identifier", "User not found");

        var response = errorHandler.handleUserServiceClientException(exception);

        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User service error: identifier=identifier, message=User not found", response.getBody().message());
    }

    @Test
    void handleNewsletterNotFoundException() {
        var exception = new NewsletterNotFoundException(1L);

        var response = errorHandler.handleNewsletterNotFoundException(exception);

        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Newsletter not found: newsletterId=1", response.getBody().message());
    }

    @Test
    void handleUserSubscriptionNotFoundException() {
        var exception = new UserSubscriptionNotFoundException(1L);

        var response = errorHandler.handleUserSubscriptionNotFoundException(exception);

        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Subscription not found: subscriptionId=1", response.getBody().message());
    }
}

package com.yurupari.notification_service.service.impl;

import com.yurupari.notification_service.client.UserServiceClient;
import com.yurupari.notification_service.service.EmailService;
import com.yurupari.notification_service.utils.TestModelFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private EmailService emailService;

    @Mock
    private UserServiceClient userServiceClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "confirmationSubscriptionSubject", "Confirm Subscription");
        ReflectionTestUtils.setField(userService, "linkPrefix", "http://localhost:8081/api/v1/subscription/confirm/");
    }

    @Test
    void sendConfirmationEmail_Success() {
        var confirmSubscriptionEvent = TestModelFactory.buildConfirmSubscriptionEvent(
                1L,
                2L,
                3L,
                "token"
        );

        var user = TestModelFactory.buildUserResponse(
                1L,
                "user@email.com",
                "firstName",
                "lastName"
        );
        when(userServiceClient.getUser(anyLong())).thenReturn(user);

        assertDoesNotThrow(() -> userService.sendConfirmationEmail(confirmSubscriptionEvent));

        verify(emailService, times(1)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void sendConfirmationEmail_Fail() {
        var confirmSubscriptionEvent = TestModelFactory.buildConfirmSubscriptionEvent(
                1L,
                2L,
                3L,
                "token"
        );

        when(userServiceClient.getUser(anyLong())).thenThrow(HttpClientErrorException.NotFound.class);

        assertDoesNotThrow(() -> userService.sendConfirmationEmail(confirmSubscriptionEvent));

        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }
}
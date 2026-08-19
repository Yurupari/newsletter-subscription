package com.yurupari.notification_service.utils;

import com.yurupari.common_data.kafka.event.ConfirmSubscriptionEvent;
import com.yurupari.notification_service.model.http.UserResponse;

public class TestModelFactory {

    public static ConfirmSubscriptionEvent buildConfirmSubscriptionEvent(
            Long subscriptionId,
            Long userId,
            Long newsletterId,
            String token
    ) {
        return ConfirmSubscriptionEvent.builder()
                .subscriptionId(subscriptionId)
                .userId(userId)
                .newsletterId(newsletterId)
                .token(token)
                .build();
    }

    public static UserResponse buildUserResponse(
            Long id,
            String email,
            String firstName,
            String lastName
    ) {
        return UserResponse.builder()
                .id(id)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }
}

package com.yurupari.subscription_service.exception;

public class UserSubscriptionExistsException extends RuntimeException {

    public UserSubscriptionExistsException(Long userId, Long newsletterId) {
        super(String.format("Subscription already exists: userId=%s, newsletterId=%s",
                userId, newsletterId));
    }
}

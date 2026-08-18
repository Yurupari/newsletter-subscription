package com.yurupari.subscription_service.exception;

public class UserSubscriptionNotFoundException extends RuntimeException {

    public UserSubscriptionNotFoundException(Long subscriptionId) {
        super(String.format("Subscription not found: subscriptionId=%d", subscriptionId));
    }
}

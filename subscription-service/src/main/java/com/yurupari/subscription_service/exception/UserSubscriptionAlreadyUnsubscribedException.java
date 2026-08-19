package com.yurupari.subscription_service.exception;

public class UserSubscriptionAlreadyUnsubscribedException extends RuntimeException {

    public UserSubscriptionAlreadyUnsubscribedException(Long id) {
        super(String.format("User subscription already unsubscribed: id=%s", id));
    }
}

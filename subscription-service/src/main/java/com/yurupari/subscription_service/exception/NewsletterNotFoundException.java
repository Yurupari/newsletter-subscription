package com.yurupari.subscription_service.exception;

public class NewsletterNotFoundException extends RuntimeException {

    public NewsletterNotFoundException(Long newsletterId) {
        super(String.format("Newsletter not found: newsletterId=%s", newsletterId));
    }
}

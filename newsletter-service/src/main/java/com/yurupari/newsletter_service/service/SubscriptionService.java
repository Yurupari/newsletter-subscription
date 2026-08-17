package com.yurupari.newsletter_service.service;

import com.yurupari.newsletter_service.model.http.request.SubscriptionRequest;
import com.yurupari.newsletter_service.model.http.response.SubscriptionResponse;

import java.util.List;

public interface SubscriptionService {
    List<SubscriptionResponse> getSubscriptions(Long userId);

    SubscriptionResponse createSubscription(Long userId, SubscriptionRequest subscriptionRequest);

    void confirmSubscription(String token);

    void deleteSubscription(Long userId, Long subscriptionId);
}

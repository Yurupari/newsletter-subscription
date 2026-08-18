package com.yurupari.subscription_service.service.impl;

import com.yurupari.subscription_service.model.http.request.SubscriptionRequest;
import com.yurupari.subscription_service.model.http.response.SubscriptionResponse;
import com.yurupari.subscription_service.model.mapper.UserSubscriptionMapper;
import com.yurupari.subscription_service.repository.UserSubscriptionRepository;
import com.yurupari.subscription_service.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final UserSubscriptionRepository userSubscriptionRepository;

    private final UserSubscriptionMapper userSubscriptionMapper;

    @Override
    public List<SubscriptionResponse> getSubscriptions(Long userId) {
        return List.of();
    }

    @Override
    @Transactional
    public SubscriptionResponse createSubscription(Long userId, SubscriptionRequest subscriptionRequest) {
        return null;
    }

    @Override
    @Transactional
    public void confirmSubscription(String token) {

    }

    @Override
    @Transactional
    public void deleteSubscription(Long userId, Long subscriptionId) {

    }
}

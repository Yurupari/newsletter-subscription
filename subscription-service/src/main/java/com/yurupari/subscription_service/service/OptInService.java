package com.yurupari.subscription_service.service;

import com.yurupari.subscription_service.model.dto.OptInDto;

import java.util.Optional;

public interface OptInService {

    Optional<OptInDto> confirmSubscription(String token);

    OptInDto getOptInBySubscriptionId(Long subscriptionId);

    OptInDto createOptIn(Long subscriptionId);
}

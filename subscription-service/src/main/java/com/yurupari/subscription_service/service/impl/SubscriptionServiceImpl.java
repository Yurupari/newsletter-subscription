package com.yurupari.subscription_service.service.impl;

import com.yurupari.subscription_service.client.UserServiceClient;
import com.yurupari.subscription_service.exception.UserSubscriptionNotFoundException;
import com.yurupari.subscription_service.messaging.kafka.SubscriptionProducer;
import com.yurupari.subscription_service.model.dto.NewsletterDto;
import com.yurupari.subscription_service.model.dto.OptInDto;
import com.yurupari.subscription_service.model.entity.UserSubscription;
import com.yurupari.subscription_service.model.enums.SubscriptionStatus;
import com.yurupari.subscription_service.model.http.request.SubscriptionRequest;
import com.yurupari.subscription_service.model.http.response.SubscriptionResponse;
import com.yurupari.subscription_service.model.mapper.UserSubscriptionMapper;
import com.yurupari.subscription_service.repository.UserSubscriptionRepository;
import com.yurupari.subscription_service.service.NewsletterService;
import com.yurupari.subscription_service.service.OptInService;
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

    private final UserServiceClient userServiceClient;

    private final NewsletterService newsletterService;

    private final OptInService optInService;

    private final SubscriptionProducer subscriptionProducer;

    private final UserSubscriptionRepository userSubscriptionRepository;

    private final UserSubscriptionMapper userSubscriptionMapper;

    @Override
    public List<SubscriptionResponse> getSubscriptions(Long userId) {
        log.info("Getting subscriptions: userId={}", userId);

        return userSubscriptionRepository.findAllSubscriptionsByUserIdAndStatusIn(
                userId, List.of(SubscriptionStatus.CONFIRMED, SubscriptionStatus.PENDING_CONFIRMATION));
    }

    @Override
    @Transactional
    public SubscriptionResponse createSubscription(Long userId, SubscriptionRequest subscriptionRequest) {
        log.info("Creating subscription: userId={}, newsletterId={}",
                userId, subscriptionRequest.newsletterId());

        userServiceClient.getUser(userId);

        final var newsletter = newsletterService.getNewsletterById(subscriptionRequest.newsletterId());

        var userSubscription = UserSubscription.builder()
                .userId(userId)
                .newsletterId(newsletter.id())
                .build();
        var savedSubscription = userSubscriptionRepository.saveAndFlush(userSubscription);

        subscriptionProducer.produceConfirmSubscriptionEvent(userSubscriptionMapper.toConfirmSubscriptionEvent(savedSubscription));

        return buildSubscriptionResponse(savedSubscription, newsletter);
    }

    @Override
    @Transactional
    public void confirmSubscription(String token) {
        log.info("Confirming subscription: token={}", token);

        final var optIn = optInService.confirmSubscription(token);

        optIn.ifPresent(this::updateSubscriptionConfirmation);
    }

    @Override
    @Transactional
    public void deleteSubscription(Long userId, Long subscriptionId) {
        log.info("Deleting subscription: userId={}, subscriptionId={}",
                userId, subscriptionId);

        userServiceClient.getUser(userId);

        var userSubscription = userSubscriptionRepository.findByIdAndUserId(subscriptionId, userId)
                .orElseThrow(() -> new UserSubscriptionNotFoundException(subscriptionId));

        userSubscription.setStatus(SubscriptionStatus.UNSUBSCRIBED);
        var updatedSubscription = userSubscriptionRepository.saveAndFlush(userSubscription);

        subscriptionProducer.produceUnsubscribeEvent(userSubscriptionMapper.toUnsubscribeEvent(updatedSubscription));
    }

    private SubscriptionResponse buildSubscriptionResponse(UserSubscription userSubscription, NewsletterDto newsletter) {
        return SubscriptionResponse.builder()
                .id(userSubscription.getUserId())
                .userId(userSubscription.getUserId())
                .newsletter(newsletter)
                .status(userSubscription.getStatus())
                .build();
    }

    private void updateSubscriptionConfirmation(OptInDto optInDto) {
        final var subscriptionId = optInDto.subscriptionId();
        log.info("Updating subscription confirmation: subscriptionId={}", subscriptionId);

        var subscription = userSubscriptionRepository.findById(subscriptionId);

        subscription.ifPresentOrElse(
                userSubscription -> {
                    if (SubscriptionStatus.PENDING_CONFIRMATION.equals(userSubscription.getStatus())) {
                        userSubscription.setStatus(SubscriptionStatus.CONFIRMED);
                        userSubscriptionRepository.saveAndFlush(userSubscription);
                    } else {
                        log.warn("Subscription cannot be confirmed: subscriptionId={}, status={}",
                                subscriptionId, userSubscription.getStatus());
                    }
                },
                () -> log.error("Subscription not found: subscriptionId={}", subscriptionId)
        );
    }
}

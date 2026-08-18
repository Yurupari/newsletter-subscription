package com.yurupari.subscription_service.service.impl;

import com.yurupari.common_data.kafka.event.ConfirmSubscriptionEvent;
import com.yurupari.subscription_service.client.UserServiceClient;
import com.yurupari.subscription_service.exception.UserSubscriptionExistsException;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

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

        var userSubscription = userSubscriptionRepository.findByUserIdAndNewsletterId(userId, subscriptionRequest.newsletterId());
        if (userSubscription.isPresent()) {
            var existingSubscription = userSubscription.get();
            switch (existingSubscription.getStatus()) {
                case PENDING_CONFIRMATION -> {
                    return processPendingSubscription(userId, subscriptionRequest, existingSubscription);
                }
                case UNSUBSCRIBED -> {
                    return processUnsubscribed(userId, subscriptionRequest, existingSubscription);
                }
                default -> {
                    throw new UserSubscriptionExistsException(userId, subscriptionRequest.newsletterId());
                }
            }
        }

        return processNewSubscription(userId, subscriptionRequest);
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

    private SubscriptionResponse processPendingSubscription(
            Long userId,
            SubscriptionRequest subscriptionRequest,
            UserSubscription existingSubscription
    ) {
        var optIn = optInService.getOptInBySubscriptionId(existingSubscription.getId());

        if (Optional.ofNullable(optIn.usedAt()).isPresent()
                || optIn.expiresAt().isBefore(Instant.now())) {
            throw new UserSubscriptionExistsException(userId, subscriptionRequest.newsletterId());
        }

        final var newsletter = newsletterService.getNewsletterById(subscriptionRequest.newsletterId());

        var newOptIn = optInService.createOptIn(existingSubscription.getId());

        var confirmSubscriptionEvent = ConfirmSubscriptionEvent.builder()
                .subscriptionId(existingSubscription.getId())
                .userId(userId)
                .newsletterId(newsletter.id())
                .token(newOptIn.token())
                .build();

        subscriptionProducer.produceConfirmSubscriptionEvent(confirmSubscriptionEvent);

        return buildSubscriptionResponse(existingSubscription, newsletter);
    }

    private SubscriptionResponse processUnsubscribed(
            Long userId,
            SubscriptionRequest subscriptionRequest,
            UserSubscription existingSubscription
    ) {
        final var newsletter = newsletterService.getNewsletterById(subscriptionRequest.newsletterId());

        existingSubscription.setStatus(SubscriptionStatus.PENDING_CONFIRMATION);
        userSubscriptionRepository.saveAndFlush(existingSubscription);

        var optIn = optInService.createOptIn(existingSubscription.getId());

        var confirmSubscriptionEvent = ConfirmSubscriptionEvent.builder()
                .subscriptionId(existingSubscription.getId())
                .userId(userId)
                .newsletterId(newsletter.id())
                .token(optIn.token())
                .build();

        subscriptionProducer.produceConfirmSubscriptionEvent(confirmSubscriptionEvent);

        return buildSubscriptionResponse(existingSubscription, newsletter);
    }

    private SubscriptionResponse processNewSubscription(
            Long userId,
            SubscriptionRequest subscriptionRequest
    ) {
        final var newsletter = newsletterService.getNewsletterById(subscriptionRequest.newsletterId());

        var newUserSubscription = UserSubscription.builder()
                .userId(userId)
                .newsletterId(newsletter.id())
                .build();
        var savedSubscription = userSubscriptionRepository.saveAndFlush(newUserSubscription);

        var optIn = optInService.createOptIn(savedSubscription.getId());

        var confirmSubscriptionEvent = ConfirmSubscriptionEvent.builder()
                .subscriptionId(savedSubscription.getId())
                .userId(userId)
                .newsletterId(newsletter.id())
                .token(optIn.token())
                .build();

        subscriptionProducer.produceConfirmSubscriptionEvent(confirmSubscriptionEvent);

        return buildSubscriptionResponse(savedSubscription, newsletter);
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

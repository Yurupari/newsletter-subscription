package com.yurupari.subscription_service.utils;

import com.yurupari.common_data.kafka.event.ConfirmSubscriptionEvent;
import com.yurupari.common_data.kafka.event.CPDEvent;
import com.yurupari.common_data.model.enums.OutboxStatus;
import com.yurupari.subscription_service.model.dto.NewsletterDto;
import com.yurupari.subscription_service.model.dto.OptInDto;
import com.yurupari.subscription_service.model.dto.OutboxEventDto;
import com.yurupari.subscription_service.model.entity.Newsletter;
import com.yurupari.subscription_service.model.entity.OptIn;
import com.yurupari.subscription_service.model.entity.OutboxEvent;
import com.yurupari.subscription_service.model.entity.UserSubscription;
import com.yurupari.subscription_service.model.enums.SubscriptionStatus;
import com.yurupari.subscription_service.model.http.request.NewsletterRequest;
import com.yurupari.subscription_service.model.http.request.SubscriptionRequest;
import com.yurupari.subscription_service.model.http.response.SubscriptionResponse;
import com.yurupari.subscription_service.model.http.response.UserResponse;

import java.time.Instant;

public class TestModelFactory {

    public static Newsletter buildNewsletter(
            Long id,
            String title,
            String description,
            Boolean isActive,
            Instant createdAt,
            Instant updatedAt
    ) {
        return Newsletter.builder()
                .id(id)
                .title(title)
                .description(description)
                .isActive(isActive)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static NewsletterDto buildNewsletterDto(
            Long id,
            String title,
            String description,
            Boolean isActive
    ) {
        return NewsletterDto.builder()
                .id(id)
                .title(title)
                .description(description)
                .isActive(isActive)
                .build();
    }

    public static UserResponse buildUser(
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

    public static UserSubscription buildUserSubscription(
            Long id,
            Long userId,
            Long newsletterId,
            SubscriptionStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        return UserSubscription.builder()
                .id(id)
                .userId(userId)
                .newsletterId(newsletterId)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static OptIn buildOptIn(
            Long id,
            Long subscriptionId,
            String token,
            Instant expiresAt
    ) {
        return OptIn.builder()
                .id(id)
                .subscriptionId(subscriptionId)
                .token(token)
                .expiresAt(expiresAt)
                .build();
    }

    public static OptIn buildOptIn(
            Long id,
            Long subscriptionId,
            String token,
            Instant expiresAt,
            Instant usedAt
    ) {
        return OptIn.builder()
                .id(id)
                .subscriptionId(subscriptionId)
                .token(token)
                .expiresAt(expiresAt)
                .usedAt(usedAt)
                .build();
    }

    public static SubscriptionResponse buildSubscriptionResponse(
            Long id,
            Long userId,
            NewsletterDto newsletter,
            SubscriptionStatus status
    ) {
        return SubscriptionResponse.builder()
                .id(id)
                .userId(userId)
                .newsletter(newsletter)
                .status(status)
                .build();
    }

    public static SubscriptionRequest buildSubscriptionRequest(Long newsletterId) {
        return SubscriptionRequest.builder()
                .newsletterId(newsletterId)
                .build();
    }

    public static OptInDto buildOptInDto(
            Long id,
            Long subscriptionId,
            String token,
            Instant expiresAt,
            Instant usedAt
    ) {
        return OptInDto.builder()
                .id(id)
                .subscriptionId(subscriptionId)
                .token(token)
                .expiresAt(expiresAt)
                .usedAt(usedAt)
                .build();
    }

    public static ConfirmSubscriptionEvent buildRegisterUserEvent(
            Long subscriptionId,
            Long userId,
            Long newsletterId
    ) {
        return ConfirmSubscriptionEvent.builder()
                .subscriptionId(subscriptionId)
                .userId(userId)
                .newsletterId(newsletterId)
                .build();
    }

    public static CPDEvent buildCPDEvent(
            Long outboxId,
            String eventType,
            String source,
            Long aggregateId,
            String properties
    ) {
        return CPDEvent.builder()
                .outboxId(outboxId)
                .eventType(eventType)
                .source(source)
                .aggregateId(aggregateId)
                .properties(properties)
                .build();
    }

    public static NewsletterRequest buildNewsletterRequest(String title, String description) {
        return NewsletterRequest.builder()
                .title(title)
                .description(description)
                .build();
    }

    public static OutboxEvent buildOutboxEvent(
            Long id,
            Long aggregateId,
            String eventType,
            String payload,
            OutboxStatus status,
            Integer retryCount,
            Instant createdAt,
            Instant processedAt
    ) {
        return OutboxEvent.builder()
                .id(id)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(payload)
                .status(status)
                .retryCount(retryCount)
                .createdAt(createdAt)
                .processedAt(processedAt)
                .build();
    }

    public static OutboxEventDto buildOutboxEventDto(
            Long id,
            Long aggregateId,
            String eventType,
            String payload,
            String status,
            Integer retryCount,
            String createdAt,
            String processedAt
    ) {
        return OutboxEventDto.builder()
                .id(id)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(payload)
                .status(status)
                .retryCount(retryCount)
                .createdAt(createdAt)
                .processedAt(processedAt)
                .build();
    }
}

package com.yurupari.subscription_service.service.impl;

import com.yurupari.subscription_service.client.UserServiceClient;
import com.yurupari.subscription_service.exception.NewsletterNotFoundException;
import com.yurupari.subscription_service.exception.UserSubscriptionExistsException;
import com.yurupari.subscription_service.exception.UserSubscriptionNotFoundException;
import com.yurupari.subscription_service.messaging.kafka.SubscriptionProducer;
import com.yurupari.subscription_service.model.enums.SubscriptionStatus;
import com.yurupari.subscription_service.model.mapper.UserSubscriptionMapperImpl;
import com.yurupari.subscription_service.repository.UserSubscriptionRepository;
import com.yurupari.subscription_service.service.NewsletterService;
import com.yurupari.subscription_service.service.OptInService;
import com.yurupari.subscription_service.utils.TestModelFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private NewsletterService newsletterService;

    @Mock
    private OptInService optInService;

    @Mock
    private SubscriptionProducer subscriptionProducer;

    @Mock
    private UserSubscriptionRepository userSubscriptionRepository;

    @Spy
    private UserSubscriptionMapperImpl userSubscriptionMapper = new UserSubscriptionMapperImpl();

    @Test
    void getSubscription_Success() {
        var newsletterDto = TestModelFactory.buildNewsletterDto(
                1L,
                "Newsletter",
                "Description",
                true
        );
        var confirmedSubscription = TestModelFactory.buildSubscriptionResponse(
                1L,
                1L,
                newsletterDto,
                SubscriptionStatus.CONFIRMED
        );
        when(userSubscriptionRepository.findAllSubscriptionsByUserIdAndStatusIn(anyLong(), anyList()))
                .thenReturn(List.of(confirmedSubscription));

        var response = subscriptionService.getSubscriptions(1L);

        assertNotNull(response);
        assertEquals(1, response.size());
    }

    @Test
    void createSubscription_Success_NewSubscription() {
        var subscriptionRequest = TestModelFactory.buildSubscriptionRequest(1L);

        var user = TestModelFactory.buildUser(
                1L,
                "email",
                "firstName",
                "lastName"
        );
        when(userServiceClient.getUser(anyLong())).thenReturn(user);

        when(userSubscriptionRepository.findByUserIdAndNewsletterId(anyLong(), anyLong())).thenReturn(Optional.empty());

        var newsletter = TestModelFactory.buildNewsletterDto(
                1L,
                "title",
                "description",
                true
        );
        when(newsletterService.getNewsletterById(anyLong())).thenReturn(newsletter);

        var userSubscription = TestModelFactory.buildUserSubscription(
                1L,
                1L,
                1L,
                null,
                null,
                null
        );
        when(userSubscriptionRepository.saveAndFlush(any())).thenReturn(userSubscription);

        var optIn = TestModelFactory.buildOptInDto(
                1L,
                1L,
                "token",
                null,
                null
        );
        when(optInService.createOptIn(anyLong())).thenReturn(optIn);

        doNothing().when(subscriptionProducer).produceConfirmSubscriptionEvent(any());

        var response = subscriptionService.createSubscription(1L, subscriptionRequest);

        verify(subscriptionProducer, times(1)).produceConfirmSubscriptionEvent(any());

        assertNotNull(response);
        assertEquals(user.id(), response.userId());
        assertEquals(newsletter.id(), response.newsletter().id());
    }

    @Test
    void createSubscription_Success_PendingSubscription() {
        var subscriptionRequest = TestModelFactory.buildSubscriptionRequest(1L);

        var user = TestModelFactory.buildUser(
                1L,
                "email",
                "firstName",
                "lastName"
        );
        when(userServiceClient.getUser(anyLong())).thenReturn(user);

        var existingSubscription = TestModelFactory.buildUserSubscription(
                1L,
                1L,
                1L,
                SubscriptionStatus.PENDING_CONFIRMATION,
                null,
                null
        );
        when(userSubscriptionRepository.findByUserIdAndNewsletterId(anyLong(), anyLong()))
                .thenReturn(Optional.of(existingSubscription));

        var optIn = TestModelFactory.buildOptInDto(
                1L,
                1L,
                "token",
                Instant.now().plusSeconds(3600),
                null
        );
        when(optInService.getOptInBySubscriptionId(anyLong())).thenReturn(optIn);

        var newsletter = TestModelFactory.buildNewsletterDto(
                1L,
                "title",
                "description",
                true
        );
        when(newsletterService.getNewsletterById(anyLong())).thenReturn(newsletter);

        var newOptIn = TestModelFactory.buildOptInDto(
                1L,
                1L,
                "new-token",
                null,
                null
        );
        when(optInService.createOptIn(anyLong())).thenReturn(newOptIn);

        doNothing().when(subscriptionProducer).produceConfirmSubscriptionEvent(any());

        var response = subscriptionService.createSubscription(1L, subscriptionRequest);

        verify(subscriptionProducer, times(1)).produceConfirmSubscriptionEvent(any());

        assertNotNull(response);
        assertEquals(user.id(), response.userId());
        assertEquals(newsletter.id(), response.newsletter().id());
    }

    @Test
    void createSubscription_Success_UnsubscribedSubscription() {
        var subscriptionRequest = TestModelFactory.buildSubscriptionRequest(1L);

        var user = TestModelFactory.buildUser(
                1L,
                "email",
                "firstName",
                "lastName"
        );
        when(userServiceClient.getUser(anyLong())).thenReturn(user);

        var existingSubscription = TestModelFactory.buildUserSubscription(
                1L,
                1L,
                1L,
                SubscriptionStatus.UNSUBSCRIBED,
                null,
                null
        );
        when(userSubscriptionRepository.findByUserIdAndNewsletterId(anyLong(), anyLong()))
                .thenReturn(Optional.of(existingSubscription));

        var newsletter = TestModelFactory.buildNewsletterDto(
                1L,
                "title",
                "description",
                true
        );
        when(newsletterService.getNewsletterById(anyLong())).thenReturn(newsletter);

        var optIn = TestModelFactory.buildOptInDto(
                1L,
                1L,
                "token",
                null,
                null
        );
        when(optInService.createOptIn(anyLong())).thenReturn(optIn);

        doNothing().when(subscriptionProducer).produceConfirmSubscriptionEvent(any());

        var response = subscriptionService.createSubscription(1L, subscriptionRequest);

        verify(subscriptionProducer, times(1)).produceConfirmSubscriptionEvent(any());
        verify(userSubscriptionRepository, times(1)).saveAndFlush(any());

        assertNotNull(response);
        assertEquals(user.id(), response.userId());
        assertEquals(newsletter.id(), response.newsletter().id());
        assertEquals(SubscriptionStatus.PENDING_CONFIRMATION, response.status());
    }

    @Test
    void createSubscription_Fail_AlreadySubscribed() {
        var subscriptionRequest = TestModelFactory.buildSubscriptionRequest(1L);

        var user = TestModelFactory.buildUser(
                1L,
                "email",
                "firstName",
                "lastName"
        );
        when(userServiceClient.getUser(anyLong())).thenReturn(user);

        var existingSubscription = TestModelFactory.buildUserSubscription(
                1L,
                1L,
                1L,
                SubscriptionStatus.CONFIRMED,
                null,
                null
        );
        when(userSubscriptionRepository.findByUserIdAndNewsletterId(anyLong(), anyLong()))
                .thenReturn(Optional.of(existingSubscription));

        assertThrows(UserSubscriptionExistsException.class,
                () -> subscriptionService.createSubscription(1L, subscriptionRequest));

        verify(subscriptionProducer, never()).produceConfirmSubscriptionEvent(any());
    }

    @Test
    void createSubscription_Fail_UserNotFound() {
        var subscriptionRequest = TestModelFactory.buildSubscriptionRequest(1L);
        when(userServiceClient.getUser(anyLong())).thenThrow(HttpClientErrorException.NotFound.class);

        assertThrows(HttpClientErrorException.NotFound.class,
                () -> subscriptionService.createSubscription(1L, subscriptionRequest));

        verify(subscriptionProducer, never()).produceConfirmSubscriptionEvent(any());
    }

    @Test
    void createSubscription_Fail_NewsletterNotFound() {
        var subscriptionRequest = TestModelFactory.buildSubscriptionRequest(1L);
        var user = TestModelFactory.buildUser(
                1L,
                "email",
                "firstName",
                "lastName"
        );
        when(userServiceClient.getUser(anyLong())).thenReturn(user);

        when(newsletterService.getNewsletterById(anyLong())).thenThrow(NewsletterNotFoundException.class);

        assertThrows(NewsletterNotFoundException.class,
                () -> subscriptionService.createSubscription(1L, subscriptionRequest));

        verify(subscriptionProducer, never()).produceConfirmSubscriptionEvent(any());
    }

    @Test
    void confirmSubscription_Success() {
        var optInDto = TestModelFactory.buildOptInDto(
                1L,
                1L,
                "token",
                null,
                null);
        when(optInService.confirmSubscription(anyString())).thenReturn(Optional.of(optInDto));

        var userSubscription = TestModelFactory.buildUserSubscription(
                1L,
                1L,
                1L,
                SubscriptionStatus.PENDING_CONFIRMATION,
                null,
                null
        );
        when(userSubscriptionRepository.findById(anyLong())).thenReturn(Optional.of(userSubscription));

        subscriptionService.confirmSubscription("token");

        verify(userSubscriptionRepository, times(1)).saveAndFlush(any());
    }

    @Test
    void confirmSubscription_Fail_OptInNotFound() {
        when(optInService.confirmSubscription(anyString())).thenReturn(Optional.empty());

        subscriptionService.confirmSubscription("token");

        verify(userSubscriptionRepository, never()).saveAndFlush(any());
    }

    @Test
    void confirmSubscription_Fail_SubscriptionNotFound() {
        var optInDto = TestModelFactory.buildOptInDto(
                1L,
                1L,
                "token",
                null,
                null
        );
        when(optInService.confirmSubscription(anyString())).thenReturn(Optional.of(optInDto));

        when(userSubscriptionRepository.findById(anyLong())).thenReturn(Optional.empty());

        subscriptionService.confirmSubscription("token");

        verify(userSubscriptionRepository, never()).saveAndFlush(any());
    }

    @Test
    void confirmSubscription_Fail_SubscriptionNotPending() {
        var optInDto = TestModelFactory.buildOptInDto(
                1L,
                1L,
                "token",
                null,
                null
        );
        when(optInService.confirmSubscription(anyString())).thenReturn(Optional.of(optInDto));

        var userSubscription = TestModelFactory.buildUserSubscription(
                1L,
                1L,
                1L,
                SubscriptionStatus.CONFIRMED,
                null,
                null
        );
        when(userSubscriptionRepository.findById(anyLong())).thenReturn(Optional.of(userSubscription));

        subscriptionService.confirmSubscription("token");

        verify(userSubscriptionRepository, never()).saveAndFlush(any());
    }

    @Test
    void deleteSubscription_Success() {
        var user = TestModelFactory.buildUser(
                1L,
                "email",
                "firstName",
                "lastName"
        );
        when(userServiceClient.getUser(anyLong())).thenReturn(user);

        var userSubscription = TestModelFactory.buildUserSubscription(
                1L,
                1L,
                1L,
                null,
                null,
                null
        );
        when(userSubscriptionRepository.findByIdAndUserId(anyLong(), anyLong()))
                .thenReturn(Optional.of(userSubscription));

        when(userSubscriptionRepository.saveAndFlush(any())).thenReturn(userSubscription);

        doNothing().when(subscriptionProducer).produceUnsubscribeEvent(any());

        subscriptionService.deleteSubscription(1L, 1L);

        verify(userSubscriptionRepository, times(1)).saveAndFlush(any());
        verify(subscriptionProducer, times(1)).produceUnsubscribeEvent(any());
    }

    @Test
    void deleteSubscription_Fail_UserNotFound() {
        when(userServiceClient.getUser(anyLong())).thenThrow(HttpClientErrorException.NotFound.class);

        assertThrows(HttpClientErrorException.NotFound.class,
                () -> subscriptionService.deleteSubscription(1L, 1L));

        verify(userSubscriptionRepository, never()).saveAndFlush(any());
        verify(subscriptionProducer, never()).produceUnsubscribeEvent(any());
    }

    @Test
    void deleteSubscription_Fail_SubscriptionNotFound() {
        var user = TestModelFactory.buildUser(
                1L,
                "email",
                "firstName",
                "lastName"
        );
        when(userServiceClient.getUser(anyLong())).thenReturn(user);

        when(userSubscriptionRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThrows(UserSubscriptionNotFoundException.class,
                () -> subscriptionService.deleteSubscription(1L, 1L));

        verify(userSubscriptionRepository, never()).saveAndFlush(any());
        verify(subscriptionProducer, never()).produceUnsubscribeEvent(any());
    }
}

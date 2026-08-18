package com.yurupari.subscription_service.controller.v1;

import com.yurupari.subscription_service.model.enums.SubscriptionStatus;
import com.yurupari.subscription_service.service.NewsletterService;
import com.yurupari.subscription_service.service.SubscriptionService;
import com.yurupari.subscription_service.utils.TestModelFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionControllerV1Test {

    @InjectMocks
    private SubscriptionControllerV1 subscriptionControllerV1;

    @Mock
    private NewsletterService newsletterService;

    @Mock
    private SubscriptionService subscriptionService;

    @Test
    void getNewsletters_Success() {
        var pageable = PageRequest.of(0, 10);
        var newsletters = IntStream.rangeClosed(1, 10)
                .mapToObj(i -> TestModelFactory.buildNewsletterDto(
                        (long) i,
                        "Newsletter " + i,
                        "Description " + i,
                        true
                ))
                .toList();
        var page = new PageImpl<>(newsletters, pageable, newsletters.size());
        when(newsletterService.getNewsletters(any())).thenReturn(page);

        var response = subscriptionControllerV1.getNewsletters(pageable);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getContent());
    }

    @Test
    void getSubscriptions_Success() {
        var newsletter = TestModelFactory.buildNewsletterDto(
                1L,
                "Newsletter 1",
                "Description 1",
                true
        );
        var subscription = TestModelFactory.buildSubscriptionResponse(
                1L,
                1L,
                newsletter,
                SubscriptionStatus.CONFIRMED
        );
        when(subscriptionService.getSubscriptions(anyLong())).thenReturn(List.of(subscription));

        var response = subscriptionControllerV1.getSubscriptions(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getSubscriptions_Empty() {
        when(subscriptionService.getSubscriptions(anyLong())).thenReturn(Collections.emptyList());

        var response = subscriptionControllerV1.getSubscriptions(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
    }

    @Test
    void subscribe_Success() {
        var request = TestModelFactory.buildSubscriptionRequest(1L);

        var newsletter = TestModelFactory.buildNewsletterDto(
                1L,
                "Newsletter 1",
                "Description 1",
                true
        );
        var subscription = TestModelFactory.buildSubscriptionResponse(
                1L,
                1L,
                newsletter,
                SubscriptionStatus.PENDING_CONFIRMATION
        );
        when(subscriptionService.createSubscription(anyLong(), any())).thenReturn(subscription);

        var response = subscriptionControllerV1.subscribe(1L, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(SubscriptionStatus.PENDING_CONFIRMATION, response.getBody().status());
    }

    @Test
    void confirmSubscription_Success() {
        doNothing().when(subscriptionService).confirmSubscription(any());

        var response = subscriptionControllerV1.confirmSubscription("token");

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void deleteSubscription_Success() {
        doNothing().when(subscriptionService).deleteSubscription(anyLong(), anyLong());

        var response = subscriptionControllerV1.deleteSubscription(1L, 1L);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}

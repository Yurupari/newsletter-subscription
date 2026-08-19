package com.yurupari.subscription_service.controller.v1;

import com.yurupari.subscription_service.model.dto.NewsletterDto;
import com.yurupari.subscription_service.model.http.request.NewsletterRequest;
import com.yurupari.subscription_service.model.http.request.SubscriptionRequest;
import com.yurupari.subscription_service.model.http.response.SubscriptionResponse;
import com.yurupari.subscription_service.service.NewsletterService;
import com.yurupari.subscription_service.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscription")
public class SubscriptionControllerV1 {

    @Autowired
    private NewsletterService newsletterService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Operation(summary = "Get all available newsletters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of all available newsletters")
    })
    @GetMapping("/newsletters")
    public ResponseEntity<Page<NewsletterDto>> getNewsletters(@ParameterObject Pageable pageable) {
        var newsletters = newsletterService.getNewsletters(pageable);
        return ResponseEntity.ok(newsletters);
    }

    @Operation(summary = "Register a newsletter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Newsletter registered successfully")
    })
    @PostMapping("/newsletter")
    public ResponseEntity<NewsletterDto> registerNewsletter(@RequestBody NewsletterRequest newsletterRequest) {
        var newsletter = newsletterService.registerNewsletter(newsletterRequest);
        return ResponseEntity.ok(newsletter);
    }

    @Operation(summary = "Delete a newsletter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Newsletter deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Newsletter not found")
    })
    @DeleteMapping("/newsletter/{newsletterId}")
    public ResponseEntity<Page<NewsletterDto>> deleteNewsletter(@PathVariable Long newsletterId) {
        newsletterService.deleteNewsletter(newsletterId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get active/pending subscriptions for an user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of subscriptions for an user"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}/subscriptions")
    public ResponseEntity<List<SubscriptionResponse>> getSubscriptions(@PathVariable Long userId) {
        var subscriptions = subscriptionService.getSubscriptions(userId);
        return ResponseEntity.ok(subscriptions);
    }

    @Operation(summary = "Initiates subscription for an user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscription initiated for an user"),
            @ApiResponse(responseCode = "400", description = "Newsletter ID must be provided"),
            @ApiResponse(responseCode = "404", description = "User or newsletter not found"),
            @ApiResponse(responseCode = "409", description = "User already subscribed to the newsletter")
    })
    @PostMapping("/user/{userId}/subscription")
    public ResponseEntity<SubscriptionResponse> subscribe(
            @PathVariable Long userId,
            @RequestBody SubscriptionRequest subscriptionRequest
    ) {
        var subscription = subscriptionService.createSubscription(userId, subscriptionRequest);
        return ResponseEntity.ok(subscription);
    }

    @Operation(summary = "Confirm subscription for an user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Subscription confirmed for an user")
    })
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmSubscription(@RequestParam String token) {
        subscriptionService.confirmSubscription(token);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete subscription for an user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Subscription deleted for an user")
    })
    @DeleteMapping("/user/{userId}/subscription/{subscriptionId}")
    public ResponseEntity<Void> deleteSubscription(
            @PathVariable Long userId,
            @PathVariable Long subscriptionId
    ) {
        subscriptionService.deleteSubscription(userId, subscriptionId);
        return ResponseEntity.noContent().build();
    }
}

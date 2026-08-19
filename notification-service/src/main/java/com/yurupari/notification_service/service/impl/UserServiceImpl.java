package com.yurupari.notification_service.service.impl;

import com.yurupari.common_data.kafka.event.ConfirmSubscriptionEvent;
import com.yurupari.notification_service.client.UserServiceClient;
import com.yurupari.notification_service.service.EmailService;
import com.yurupari.notification_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final EmailService emailService;

    private final UserServiceClient userServiceClient;

    @Value("${email.confirmation-subscription.subject}")
    private String confirmationSubscriptionSubject;

    @Value("${email.confirmation-subscription.link-prefix}")
    private String linkPrefix;

    @Override
    public void sendConfirmationEmail(ConfirmSubscriptionEvent confirmSubscriptionEvent) {
        var userId = confirmSubscriptionEvent.userId();
        log.info("Sending confirmation email to user: userId={}, subscriptionId={}",
                userId, confirmSubscriptionEvent.subscriptionId());

        try {
            var user = userServiceClient.getUser(userId);

            var body = linkPrefix + confirmSubscriptionEvent.token();

            emailService.sendEmail(
                    user.email(),
                    confirmationSubscriptionSubject,
                    body
            );
        } catch (Exception e) {
            log.warn("Failed to send confirmation email to user", e);
        }
    }
}

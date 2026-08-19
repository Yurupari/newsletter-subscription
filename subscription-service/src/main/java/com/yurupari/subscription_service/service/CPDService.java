package com.yurupari.subscription_service.service;

public interface CPDService {

    void sendSubscriptionCPDNotification(Long userId, Long newsletterId, boolean isDoubleOptInConfirmed);

    void sendUnsubscriptionCPDNotification(Long userId, Long newsletterId, boolean isDoubleOptInConfirmed);
}

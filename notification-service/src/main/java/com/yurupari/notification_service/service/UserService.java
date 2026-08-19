package com.yurupari.notification_service.service;

import com.yurupari.common_data.kafka.event.ConfirmSubscriptionEvent;

public interface UserService {

    void sendConfirmationEmail(ConfirmSubscriptionEvent confirmSubscriptionEvent);
}

package com.yurupari.cpd_service.model.enums;

import java.util.Optional;

public enum OutboxEventType {
    NEWSLETTER_SUBSCRIBED,
    NEWSLETTER_UNSUBSCRIBED,
    USER_CREATED,
    USER_UPDATED,
    USER_DELETED,
    UNKNOWN;

    public static OutboxEventType from(String type) {
        return Optional.ofNullable(type)
                .map(t -> {
                    try {
                        return OutboxEventType.valueOf(type.trim().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        return UNKNOWN;
                    }
                })
                .orElse(UNKNOWN);
    }
}

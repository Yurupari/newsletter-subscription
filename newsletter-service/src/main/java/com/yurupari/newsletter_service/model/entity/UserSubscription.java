package com.yurupari.newsletter_service.model.entity;

import com.yurupari.common_data.model.entity.BaseEntity;
import com.yurupari.newsletter_service.model.enums.SubscriptionStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "user_subscription")
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@DynamicUpdate
public class UserSubscription extends BaseEntity {

    private Long userId;

    private Long newsletterId;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;
}

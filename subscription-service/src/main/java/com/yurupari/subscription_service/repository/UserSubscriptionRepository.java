package com.yurupari.subscription_service.repository;

import com.yurupari.subscription_service.model.entity.UserSubscription;
import com.yurupari.subscription_service.model.enums.SubscriptionStatus;
import com.yurupari.subscription_service.model.http.response.SubscriptionResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

    @Query("""
        SELECT new com.yurupari.subscription_service.model.http.response.SubscriptionResponse(
            s.id,
            s.userId,
            new com.yurupari.subscription_service.model.dto.NewsletterDto(
                n.id,
                n.title,
                n.description,
                n.isActive
            ),
            s.status
        )
        FROM UserSubscription s
        JOIN Newsletter n ON s.newsletterId = n.id
        WHERE s.userId = :userId
        AND s.status IN :statuses
    """)
    List<SubscriptionResponse> findAllSubscriptionsByUserIdAndStatusIn(
            @Param("userId") Long userId,
            @Param("statuses") List<SubscriptionStatus> statuses
    );

    Optional<UserSubscription> findByIdAndUserId(Long id, Long userId);

    Optional<UserSubscription> findByUserIdAndNewsletterId(Long userId, Long newsletterId);
}

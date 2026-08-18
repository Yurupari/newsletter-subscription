package com.yurupari.subscription_service.repository;

import com.yurupari.subscription_service.model.entity.OptIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OptInRepository extends JpaRepository<OptIn, Long> {

    Optional<OptIn> findByToken(String token);

    Optional<OptIn> findBySubscriptionId(Long subscriptionId);
}

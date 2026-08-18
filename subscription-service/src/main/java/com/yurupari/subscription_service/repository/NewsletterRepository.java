package com.yurupari.subscription_service.repository;

import com.yurupari.subscription_service.model.entity.Newsletter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsletterRepository extends JpaRepository<Newsletter, Long> {
}

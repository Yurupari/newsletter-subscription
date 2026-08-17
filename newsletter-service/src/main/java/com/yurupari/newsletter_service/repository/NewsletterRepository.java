package com.yurupari.newsletter_service.repository;

import com.yurupari.newsletter_service.model.entity.Newsletter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsletterRepository extends JpaRepository<Newsletter, Long> {
}

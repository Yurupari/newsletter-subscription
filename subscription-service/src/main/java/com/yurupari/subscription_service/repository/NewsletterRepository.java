package com.yurupari.subscription_service.repository;

import com.yurupari.subscription_service.model.entity.Newsletter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewsletterRepository extends JpaRepository<Newsletter, Long> {

    Page<Newsletter> findByIsActive(Boolean isActive, Pageable pageable);

    Optional<Newsletter> findByIdAndIsActive(Long id, boolean b);
}

package com.yurupari.subscription_service.service;

import com.yurupari.subscription_service.model.dto.NewsletterDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NewsletterService {
    Page<NewsletterDto> getNewsletters(Pageable pageable);
}

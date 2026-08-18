package com.yurupari.subscription_service.service;

import com.yurupari.subscription_service.model.dto.NewsletterDto;
import com.yurupari.subscription_service.model.http.request.NewsletterRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NewsletterService {

    Page<NewsletterDto> getNewsletters(Pageable pageable);

    NewsletterDto getNewsletterById(Long id);

    NewsletterDto registerNewsletter(NewsletterRequest newsletterRequest);

    void deleteNewsletter(Long id);
}

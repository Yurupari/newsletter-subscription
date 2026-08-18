package com.yurupari.subscription_service.service.impl;

import com.yurupari.subscription_service.model.dto.NewsletterDto;
import com.yurupari.subscription_service.model.mapper.NewsletterMapper;
import com.yurupari.subscription_service.repository.NewsletterRepository;
import com.yurupari.subscription_service.service.NewsletterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsletterServiceImpl implements NewsletterService {

    private final NewsletterRepository newsletterRepository;

    private final NewsletterMapper newsletterMapper;

    @Override
    public Page<NewsletterDto> getNewsletters(Pageable pageable) {
        return null;
    }
}

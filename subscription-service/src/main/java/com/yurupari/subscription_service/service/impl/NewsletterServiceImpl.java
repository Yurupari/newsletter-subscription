package com.yurupari.subscription_service.service.impl;

import com.yurupari.subscription_service.exception.NewsletterNotFoundException;
import com.yurupari.subscription_service.model.dto.NewsletterDto;
import com.yurupari.subscription_service.model.http.request.NewsletterRequest;
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
        log.info("Getting newsletters: pageNumber={}, pageSize={}",
                pageable.getPageNumber(), pageable.getPageSize());

        final var newsletters = newsletterRepository.findByIsActive(true, pageable);

        return newsletters.map(newsletterMapper::toDto);
    }

    @Override
    public NewsletterDto getNewsletterById(Long id) {
        log.info("Getting newsletter: id={}", id);

        final var newsletter = newsletterRepository.findByIdAndIsActive(id, true)
                .orElseThrow(() -> new NewsletterNotFoundException(id));

        return newsletterMapper.toDto(newsletter);
    }

    @Override
    public NewsletterDto registerNewsletter(NewsletterRequest newsletterRequest) {
        log.info("Registering newsletter: request={}", newsletterRequest);

        final var newsletter = newsletterMapper.toEntity(newsletterRequest);
        var savedNewsletter = newsletterRepository.saveAndFlush(newsletter);

        return newsletterMapper.toDto(savedNewsletter);
    }

    @Override
    public void deleteNewsletter(Long id) {
        log.info("Deleting newsletter: newsletterId={}", id);

        var newsletter = newsletterRepository.findById(id)
                .orElseThrow(() -> new NewsletterNotFoundException(id));

        newsletter.setIsActive(false);
        newsletterRepository.saveAndFlush(newsletter);
    }
}

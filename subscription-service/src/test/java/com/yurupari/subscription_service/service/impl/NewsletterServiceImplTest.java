package com.yurupari.subscription_service.service.impl;

import com.yurupari.subscription_service.exception.NewsletterNotFoundException;
import com.yurupari.subscription_service.model.mapper.NewsletterMapperImpl;
import com.yurupari.subscription_service.repository.NewsletterRepository;
import com.yurupari.subscription_service.utils.TestModelFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsletterServiceImplTest {

    @InjectMocks
    private NewsletterServiceImpl newsletterService;

    @Mock
    private NewsletterRepository newsletterRepository;

    @Spy
    private NewsletterMapperImpl newsletterMapper = new NewsletterMapperImpl();

    @Test
    void getNewsletters_Success() {
        var pageable = PageRequest.of(0, 10);
        var newsletters = IntStream.rangeClosed(1, 10)
                .mapToObj(i -> TestModelFactory.buildNewsletter(
                        null,
                        "Newsletter " + i,
                        "Description " + i,
                        true,
                        Instant.now(),
                        Instant.now()
                ))
                .toList();
        var page = new PageImpl<>(newsletters, pageable, newsletters.size());
        when(newsletterRepository.findByIsActive(anyBoolean(), any())).thenReturn(page);

        var response = newsletterService.getNewsletters(pageable);

        assertNotNull(response);
        assertEquals(10, response.stream().count());
    }

    @Test
    void getNewslettersById_Success() {
        var newsletter = TestModelFactory.buildNewsletter(
                1L,
                "Newsletter",
                "Description",
                true,
                Instant.now(),
                Instant.now()
        );
        when(newsletterRepository.findByIdAndIsActive(any(), anyBoolean())).thenReturn(Optional.of(newsletter));

        var response = newsletterService.getNewsletterById(1L);

        assertNotNull(response);
        assertTrue(response.isPresent());
        assertEquals(newsletter.getId(), response.get().id());
        assertEquals(newsletter.getTitle(), response.get().title());
        assertEquals(newsletter.getDescription(), response.get().description());
        assertEquals(newsletter.getIsActive(), response.get().isActive());
    }

    @Test
    void getNewslettersById_NotFound() {
        when(newsletterRepository.findByIdAndIsActive(any(), anyBoolean())).thenReturn(Optional.empty());

        var response = newsletterService.getNewsletterById(1L);

        assertNotNull(response);
        assertFalse(response.isPresent());
    }

    @Test
    void registerNewsletter_Success() {
        var request = TestModelFactory.buildNewsletterRequest(
                "Newsletter",
                "Description"
        );

        var newsletter = TestModelFactory.buildNewsletter(
                1L,
                "Newsletter",
                "Description",
                true,
                Instant.now(),
                Instant.now()
        );
        when(newsletterRepository.saveAndFlush(any())).thenReturn(newsletter);

        var response = newsletterService.registerNewsletter(request);

        assertNotNull(response);
    }

    @Test
    void deleteNewsletter_Success() {
        var newsletter = TestModelFactory.buildNewsletter(
                1L,
                "Newsletter",
                "Description",
                true,
                Instant.now(),
                Instant.now()
        );
        when(newsletterRepository.findById(any())).thenReturn(Optional.of(newsletter));

        newsletterService.deleteNewsletter(1L);

        assertEquals(false, newsletter.getIsActive());

        verify(newsletterRepository, times(1)).saveAndFlush(any());
    }

    @Test
    void deleteNewsletter_NotFound() {
        when(newsletterRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(NewsletterNotFoundException.class, () -> newsletterService.deleteNewsletter(1L));

        verify(newsletterRepository, never()).saveAndFlush(any());
    }
}
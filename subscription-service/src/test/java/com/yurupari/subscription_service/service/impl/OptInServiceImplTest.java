package com.yurupari.subscription_service.service.impl;

import com.yurupari.subscription_service.exception.OptInNotFoundException;
import com.yurupari.subscription_service.model.mapper.OptInMapperImpl;
import com.yurupari.subscription_service.repository.OptInRepository;
import com.yurupari.subscription_service.utils.TestModelFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OptInServiceImplTest {

    @InjectMocks
    private OptInServiceImpl optInService;

    @Mock
    private OptInRepository optInRepository;

    @Spy
    private OptInMapperImpl optInMapper = new OptInMapperImpl();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(optInService, "tokenValidTime", 3600L);
    }

    @Test
    void confirmSubscription_Success() {
        var expiredAt = Instant.now().plusSeconds(3600);
        var optIn = TestModelFactory.buildOptIn(
                1L,
                1L,
                "token",
                expiredAt,
                null
        );
        when(optInRepository.findByToken(anyString())).thenReturn(Optional.of(optIn));

        var updatedOptIn = TestModelFactory.buildOptIn(
                1L,
                1L,
                "token",
                expiredAt,
                Instant.now()
        );
        when(optInRepository.saveAndFlush(any())).thenReturn(updatedOptIn);

        var response = optInService.confirmSubscription("token");

        verify(optInRepository, times(1)).saveAndFlush(any());

        assertNotNull(response);
        assertTrue(response.isPresent());
    }

    @Test
    void confirmSubscription_Fail_TokenNotFound() {
        when(optInRepository.findByToken(anyString())).thenReturn(Optional.empty());

        var response = optInService.confirmSubscription("token");

        verify(optInRepository, never()).saveAndFlush(any());

        assertNotNull(response);
        assertFalse(response.isPresent());
    }

    @Test
    void confirmSubscription_Fail_TokenAlreadyUsed() {
        var optIn = TestModelFactory.buildOptIn(
                1L,
                1L,
                "token",
                Instant.now().plusSeconds(3600),
                Instant.now()
        );
        when(optInRepository.findByToken(anyString())).thenReturn(Optional.of(optIn));

        var response = optInService.confirmSubscription("token");

        verify(optInRepository, never()).saveAndFlush(any());

        assertNotNull(response);
        assertFalse(response.isPresent());
    }

    @Test
    void confirmSubscription_Fail_TokenExpired() {
        var optIn = TestModelFactory.buildOptIn(
                1L,
                1L,
                "token",
                Instant.now().minusSeconds(3600),
                null
        );
        when(optInRepository.findByToken(anyString())).thenReturn(Optional.of(optIn));

        var response = optInService.confirmSubscription("token");

        verify(optInRepository, never()).saveAndFlush(any());

        assertNotNull(response);
        assertFalse(response.isPresent());
    }

    @Test
    void getOptInBySubscriptionId_Success() {
        var optIn = TestModelFactory.buildOptIn(
                1L,
                1L,
                "token",
                Instant.now().plusSeconds(3600),
                null
        );
        when(optInRepository.findBySubscriptionId(anyLong())).thenReturn(Optional.of(optIn));

        var response = optInService.getOptInBySubscriptionId(1L);

        assertNotNull(response);
    }

    @Test
    void getOptInBySubscriptionId_Fail_NotFound() {
        when(optInRepository.findBySubscriptionId(anyLong())).thenReturn(Optional.empty());

        assertThrows(OptInNotFoundException.class, () -> optInService.getOptInBySubscriptionId(1L));
    }

    @Test
    void createOptIn_Success() {
        var optIn = TestModelFactory.buildOptIn(
                1L,
                1L,
                "token",
                Instant.now().plusSeconds(3600),
                null
        );
        when(optInRepository.saveAndFlush(any())).thenReturn(optIn);

        var response = optInService.createOptIn(1L);

        assertNotNull(response);
    }
}

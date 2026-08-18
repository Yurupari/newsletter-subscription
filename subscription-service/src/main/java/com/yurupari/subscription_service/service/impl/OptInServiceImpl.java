package com.yurupari.subscription_service.service.impl;

import com.yurupari.subscription_service.exception.OptInNotFoundException;
import com.yurupari.subscription_service.model.dto.OptInDto;
import com.yurupari.subscription_service.model.entity.OptIn;
import com.yurupari.subscription_service.model.mapper.OptInMapper;
import com.yurupari.subscription_service.repository.OptInRepository;
import com.yurupari.subscription_service.service.OptInService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OptInServiceImpl implements OptInService {

    private final OptInRepository optInRepository;

    private final OptInMapper optInMapper;

    @Value("${token.valid-time}")
    private Long tokenValidTime;

    @Override
    @Transactional
    public Optional<OptInDto> confirmSubscription(String token) {
        log.info("Find and update opt-in: token={}", token);

        final var now = Instant.now();

        var optIn = optInRepository.findByToken(token);

        return optIn
                .map(optInEntity -> {
                    if (optInEntity.getUsedAt() != null) {
                        log.warn("Opt-in already used: token={}, subscriptionId={}",
                                token, optInEntity.getSubscriptionId());
                        return Optional.<OptInDto>empty();
                    }

                    if (optInEntity.getExpiresAt().isBefore(now)) {
                        log.warn("Opt-in expired: token={}, subscriptionId={}",
                                token, optInEntity.getSubscriptionId());
                        return Optional.<OptInDto>empty();
                    }

                    optInEntity.setUsedAt(now);
                    var updatedOptIn = optInRepository.saveAndFlush(optInEntity);

                    return Optional.of(optInMapper.toDto(updatedOptIn));
                })
                .orElseGet(() -> {
                    log.warn("Opt-in not found: token={}", token);
                    return Optional.empty();
                });
    }

    @Override
    public OptInDto getOptInBySubscriptionId(Long subscriptionId) {
        log.info("Find opt-in: subscriptionId={}", subscriptionId);

        var optIn = optInRepository.findBySubscriptionId(subscriptionId)
                .orElseThrow(() -> new OptInNotFoundException(String.format("Opt-in not found: subscriptionId=%s",
                        subscriptionId)));

        return optInMapper.toDto(optIn);
    }

    @Override
    public OptInDto createOptIn(Long subscriptionId) {
        log.info("Creating opt-in: subscriptionId={}", subscriptionId);

        var optIn = OptIn.builder()
                .subscriptionId(subscriptionId)
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plusSeconds(tokenValidTime))
                .build();
        var savedOptIn = optInRepository.saveAndFlush(optIn);

        return optInMapper.toDto(savedOptIn);
    }
}

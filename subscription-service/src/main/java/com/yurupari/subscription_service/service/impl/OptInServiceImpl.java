package com.yurupari.subscription_service.service.impl;

import com.yurupari.subscription_service.model.dto.OptInDto;
import com.yurupari.subscription_service.model.mapper.OptInMapper;
import com.yurupari.subscription_service.repository.OptInRepository;
import com.yurupari.subscription_service.service.OptInService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OptInServiceImpl implements OptInService {

    private final OptInRepository optInRepository;

    private final OptInMapper optInMapper;

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
}

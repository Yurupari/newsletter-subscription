package com.yurupari.subscription_service.model.mapper;

import com.yurupari.common_data.config.CentralMapperConfig;
import com.yurupari.subscription_service.model.dto.NewsletterDto;
import com.yurupari.subscription_service.model.entity.Newsletter;
import com.yurupari.subscription_service.model.http.request.NewsletterRequest;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        config = CentralMapperConfig.class
)
public interface NewsletterMapper {

    NewsletterDto toDto(Newsletter newsletter);

    Newsletter toEntity(NewsletterDto newsletterDto);

    Newsletter toEntity(NewsletterRequest newsletterRequest);
}

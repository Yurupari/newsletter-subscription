package com.yurupari.newsletter_service.model.mapper;

import com.yurupari.common_data.config.CentralMapperConfig;
import com.yurupari.newsletter_service.model.dto.NewsletterDto;
import com.yurupari.newsletter_service.model.entity.Newsletter;
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
}

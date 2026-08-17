package com.yurupari.newsletter_service.model.mapper;

import com.yurupari.common_data.config.CentralMapperConfig;
import com.yurupari.newsletter_service.model.dto.UserSubscriptionDto;
import com.yurupari.newsletter_service.model.entity.UserSubscription;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        config = CentralMapperConfig.class
)
public interface UserSubscriptionMapper {

    UserSubscriptionDto toDto(UserSubscription userSubscription);

    UserSubscription toEntity(UserSubscriptionDto userSubscriptionDto);
}

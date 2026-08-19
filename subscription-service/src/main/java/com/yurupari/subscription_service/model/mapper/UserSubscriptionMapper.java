package com.yurupari.subscription_service.model.mapper;

import com.yurupari.common_data.config.CentralMapperConfig;
import com.yurupari.common_data.kafka.event.ConfirmSubscriptionEvent;
import com.yurupari.subscription_service.model.dto.UserSubscriptionDto;
import com.yurupari.subscription_service.model.entity.UserSubscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        config = CentralMapperConfig.class
)
public interface UserSubscriptionMapper {

    UserSubscriptionDto toDto(UserSubscription userSubscription);

    UserSubscription toEntity(UserSubscriptionDto userSubscriptionDto);

    @Mapping(target = "subscriptionId", source = "userSubscription.id")
    ConfirmSubscriptionEvent toConfirmSubscriptionEvent(UserSubscription userSubscription);
}

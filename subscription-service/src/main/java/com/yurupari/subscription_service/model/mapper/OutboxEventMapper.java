package com.yurupari.subscription_service.model.mapper;

import com.yurupari.common_data.config.CentralMapperConfig;
import com.yurupari.subscription_service.model.dto.OutboxEventDto;
import com.yurupari.subscription_service.model.entity.OutboxEvent;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        config = CentralMapperConfig.class
)
public interface OutboxEventMapper {

    OutboxEventDto toDto(OutboxEvent entity);

    OutboxEvent toEntity(OutboxEventDto dto);
}

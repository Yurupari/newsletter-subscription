package com.yurupari.subscription_service.model.mapper;

import com.yurupari.subscription_service.model.dto.OptInDto;
import com.yurupari.subscription_service.model.entity.OptIn;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface OptInMapper {

    OptInDto toDto(OptIn optIn);
}

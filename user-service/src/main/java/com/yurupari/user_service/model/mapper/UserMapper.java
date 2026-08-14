package com.yurupari.user_service.model.mapper;

import com.yurupari.common_data.config.CentralMapperConfig;
import com.yurupari.user_service.model.dto.UserDto;
import com.yurupari.user_service.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        config = CentralMapperConfig.class
)
public interface UserMapper {

    UserDto toDto(User user);

    @Mapping(target = "status", defaultValue = "ACTIVE")
    User toEntity(UserDto userDto);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(UserDto userDto, @MappingTarget User user);
}

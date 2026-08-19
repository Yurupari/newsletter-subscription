package com.yurupari.user_service.model.mapper;

import com.yurupari.common_data.config.CentralMapperConfig;
import com.yurupari.user_service.kafka.event.DeleteUserEvent;
import com.yurupari.user_service.kafka.event.RegisterUserEvent;
import com.yurupari.user_service.model.dto.UserDto;
import com.yurupari.user_service.model.entity.User;
import com.yurupari.user_service.model.http.request.UserUpdateRequest;
import com.yurupari.user_service.model.http.response.UserResponse;
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

    @Mapping(target = "userId", source = "user.id")
    RegisterUserEvent toRegisterUserEvent(User user);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "keycloakUserId", source = "user.authUserId")
    DeleteUserEvent toDeleteUserEvent(User user);

    UserResponse toUserResponse(User user);

    User toEntity(UserDto userDto);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(UserDto userDto, @MappingTarget User user);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateEntityFromUserRequest(UserUpdateRequest userUpdateRequest, @MappingTarget User user);
}

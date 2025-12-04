package com.loievroman.carsharingapp.mapper;

import com.loievroman.carsharingapp.config.MapperConfig;
import com.loievroman.carsharingapp.dto.user.UserProfileResponseDto;
import com.loievroman.carsharingapp.dto.user.UserRegistrationRequestDto;
import com.loievroman.carsharingapp.dto.user.UserResponseDto;
import com.loievroman.carsharingapp.model.User;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    UserResponseDto toUserResponse(User user);

    @org.mapstruct.Mapping(target = "id", ignore = true)
    @org.mapstruct.Mapping(target = "deleted", ignore = true)
    @org.mapstruct.Mapping(target = "roles", ignore = true)
    @org.mapstruct.Mapping(target = "authorities", ignore = true)
    User toEntity(UserRegistrationRequestDto userRegistrationRequestDto);

    UserProfileResponseDto toProfileDto(User user);
}

package com.loievroman.carsharingapp.mapper;

import com.loievroman.carsharingapp.config.MapperConfig;
import com.loievroman.carsharingapp.dto.user.UserRegistrationRequestDto;
import com.loievroman.carsharingapp.dto.user.UserResponseDto;
import com.loievroman.carsharingapp.model.User;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    UserResponseDto toUserResponse(User user);

    User toEntity(UserRegistrationRequestDto userRegistrationRequestDto);
}

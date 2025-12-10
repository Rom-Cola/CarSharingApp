package com.loievroman.carsharingapp.mapper;

import com.loievroman.carsharingapp.config.MapperConfig;
import com.loievroman.carsharingapp.dto.user.UserProfileResponseDto;
import com.loievroman.carsharingapp.dto.user.UserRegistrationRequestDto;
import com.loievroman.carsharingapp.dto.user.UserResponseDto;
import com.loievroman.carsharingapp.model.Role;
import com.loievroman.carsharingapp.model.User;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    UserResponseDto toUserResponse(User user);

    @org.mapstruct.Mapping(target = "id", ignore = true)
    @org.mapstruct.Mapping(target = "deleted", ignore = true)
    @org.mapstruct.Mapping(target = "roles", ignore = true)
    @org.mapstruct.Mapping(target = "authorities", ignore = true)
    User toEntity(UserRegistrationRequestDto userRegistrationRequestDto);

    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToRoleNames")
    UserProfileResponseDto toProfileDto(User user);

    @Named("rolesToRoleNames")
    default Set<Role.RoleName> rolesToRoleNames(Set<Role> roles) {
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}

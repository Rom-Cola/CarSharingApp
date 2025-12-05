package com.loievroman.carsharingapp.service;

import com.loievroman.carsharingapp.dto.user.UserProfileResponseDto;
import com.loievroman.carsharingapp.dto.user.UserProfileUpdateRequestDto;
import com.loievroman.carsharingapp.dto.user.UserRegistrationRequestDto;
import com.loievroman.carsharingapp.dto.user.UserResponseDto;
import com.loievroman.carsharingapp.dto.user.UserRoleUpdateRequestDto;
import com.loievroman.carsharingapp.exception.RegistrationException;
import com.loievroman.carsharingapp.model.User;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto requestDto) throws RegistrationException;

    UserProfileResponseDto updateRoles(Long userId, UserRoleUpdateRequestDto requestDto);

    UserProfileResponseDto getMyProfile(User user);

    UserProfileResponseDto updateMyProfile(User user,
                                           UserProfileUpdateRequestDto requestDto);

    boolean existsById(Long userId);
}

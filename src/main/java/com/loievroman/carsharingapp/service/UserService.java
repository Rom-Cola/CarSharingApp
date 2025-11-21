package com.loievroman.carsharingapp.service;

import com.loievroman.carsharingapp.dto.user.UserProfileResponseDto;
import com.loievroman.carsharingapp.dto.user.UserProfileUpdateRequestDto;
import com.loievroman.carsharingapp.dto.user.UserRegistrationRequestDto;
import com.loievroman.carsharingapp.dto.user.UserResponseDto;
import com.loievroman.carsharingapp.dto.user.UserRoleUpdateRequestDto;
import com.loievroman.carsharingapp.exception.RegistrationException;
import org.springframework.security.core.Authentication;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto requestDto) throws RegistrationException;

    UserProfileResponseDto updateRole(Long userId, UserRoleUpdateRequestDto requestDto);

    UserProfileResponseDto getMyProfile(Authentication authentication);

    UserProfileResponseDto updateMyProfile(Authentication authentication,
                                           UserProfileUpdateRequestDto requestDto);

    UserProfileResponseDto addRoleToUser(Long userId, UserRoleUpdateRequestDto requestDto);

    UserProfileResponseDto removeRoleFromUser(Long userId, UserRoleUpdateRequestDto requestDto);
}

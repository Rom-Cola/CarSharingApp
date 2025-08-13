package com.loievroman.carsharingapp.service;

import com.loievroman.carsharingapp.dto.user.UserRegistrationRequestDto;
import com.loievroman.carsharingapp.dto.user.UserResponseDto;
import com.loievroman.carsharingapp.exception.RegistrationException;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto requestDto) throws RegistrationException;
}

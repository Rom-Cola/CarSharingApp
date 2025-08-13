package com.loievroman.carsharingapp.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginResponseDto {
    private final String token;

    public UserLoginResponseDto(String token) {
        this.token = token;
    }
}

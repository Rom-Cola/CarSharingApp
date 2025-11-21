package com.loievroman.carsharingapp.dto.user;

import lombok.Data;

@Data
public class UserProfileResponseDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
}

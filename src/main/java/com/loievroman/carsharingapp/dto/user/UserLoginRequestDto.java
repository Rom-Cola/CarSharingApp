package com.loievroman.carsharingapp.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequestDto {
    @NotBlank
    @Size(min = 8, max = 100)
    @Email
    private String email;
    @NotEmpty
    @Size(min = 8, max = 20)
    private String password;
}

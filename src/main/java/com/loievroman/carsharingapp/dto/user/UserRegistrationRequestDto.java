package com.loievroman.carsharingapp.dto.user;

import com.loievroman.carsharingapp.validation.fieldmatch.FieldMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@FieldMatch(first = "password", second = "repeatPassword", message = "Passwords do not match")
public class UserRegistrationRequestDto {
    @NotBlank
    @Size(min = 8, max = 100)
    @Email
    private String email;
    @Size(min = 8, max = 20)
    @NotBlank
    private String password;
    @Size(min = 8, max = 20)
    @NotBlank
    private String repeatPassword;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;

}

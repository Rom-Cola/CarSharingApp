package com.loievroman.carsharingapp.dto.user;

import com.loievroman.carsharingapp.model.Role;
import java.util.Set;
import lombok.Data;

@Data
public class UserProfileResponseDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Set<Role.RoleName> roles;
}

package com.loievroman.carsharingapp.dto.user;

import com.loievroman.carsharingapp.model.Role;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import lombok.Data;

@Data
public class UserRoleUpdateRequestDto {
    @NotEmpty
    private Set<Role.RoleName> roles;
}

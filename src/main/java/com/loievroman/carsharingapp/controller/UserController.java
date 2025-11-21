package com.loievroman.carsharingapp.controller;

import com.loievroman.carsharingapp.dto.user.UserProfileResponseDto;
import com.loievroman.carsharingapp.dto.user.UserProfileUpdateRequestDto;
import com.loievroman.carsharingapp.dto.user.UserRoleUpdateRequestDto;
import com.loievroman.carsharingapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('MANAGER')")
    public UserProfileResponseDto updateUserRole(
            @PathVariable Long id,
            @RequestBody @Valid UserRoleUpdateRequestDto requestDto
    ) {
        return userService.updateRole(id, requestDto);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserProfileResponseDto getMyProfile(Authentication authentication) {
        return userService.getMyProfile(authentication);
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserProfileResponseDto updateMyProfile(
            Authentication authentication,
            @RequestBody @Valid UserProfileUpdateRequestDto requestDto
    ) {
        return userService.updateMyProfile(authentication, requestDto);
    }

    @PostMapping("/{id}/role") // Додає роль
    @PreAuthorize("hasRole('MANAGER')")
    public UserProfileResponseDto addRole(
            @PathVariable Long id,
            @RequestBody @Valid UserRoleUpdateRequestDto requestDto
    ) {
        return userService.addRoleToUser(id, requestDto);
    }

    @DeleteMapping("/{id}/role") // Видаляє роль
    @PreAuthorize("hasRole('MANAGER')")
    public UserProfileResponseDto removeRole(
            @PathVariable Long id,
            @RequestBody @Valid UserRoleUpdateRequestDto requestDto
    ) {
        return userService.removeRoleFromUser(id, requestDto);
    }

}

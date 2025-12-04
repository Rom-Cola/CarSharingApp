package com.loievroman.carsharingapp.controller;

import com.loievroman.carsharingapp.dto.user.UserProfileResponseDto;
import com.loievroman.carsharingapp.dto.user.UserProfileUpdateRequestDto;
import com.loievroman.carsharingapp.dto.user.UserRoleUpdateRequestDto;
import com.loievroman.carsharingapp.model.User;
import com.loievroman.carsharingapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Management", description = "Endpoints for managing user profiles and roles")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Update user roles (Admin)",
            description = "Sets a new collection of roles for a specific user. "
                    + "Replaces all existing roles.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Roles updated successfully"),
            @ApiResponse(responseCode = "403",
                    description = "Access denied"),
            @ApiResponse(responseCode = "404",
                    description = "User or Role not found")
    })
    public UserProfileResponseDto updateUserRoles(
            @PathVariable Long id,
            @RequestBody @Valid UserRoleUpdateRequestDto requestDto
    ) {
        return userService.updateRoles(id, requestDto);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(
            summary = "Get my profile info",
            description = "Retrieves the profile information for the currently authenticated user."
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved profile information")
    public UserProfileResponseDto getMyProfile(@AuthenticationPrincipal User currentUser) {
        return userService.getMyProfile(currentUser);
    }

    @PatchMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Update my profile",
            description = "Allows the authenticated user to update their profile information.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data provided for update")
    })
    public UserProfileResponseDto updateMyProfile(
            @AuthenticationPrincipal User currentUser,
            @RequestBody @Valid UserProfileUpdateRequestDto requestDto
    ) {
        return userService.updateMyProfile(currentUser, requestDto);
    }

}

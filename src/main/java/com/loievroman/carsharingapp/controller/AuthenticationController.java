package com.loievroman.carsharingapp.controller;

import com.loievroman.carsharingapp.dto.user.UserLoginRequestDto;
import com.loievroman.carsharingapp.dto.user.UserLoginResponseDto;
import com.loievroman.carsharingapp.dto.user.UserRegistrationRequestDto;
import com.loievroman.carsharingapp.dto.user.UserResponseDto;
import com.loievroman.carsharingapp.exception.RegistrationException;
import com.loievroman.carsharingapp.security.AuthenticationService;
import com.loievroman.carsharingapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication Management", description = "Endpoints for user registration and login")
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    @PostMapping("/registration")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user",
            description = "Creates a new user account. Upon successful registration, "
                    + "the user is assigned the 'CUSTOMER' role by default.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "User registered successfully"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid input data"
                            + " (e.g., passwords don't match, invalid email)"),
            @ApiResponse(responseCode = "409",
                    description = "A user with this email already exists")
    })
    public UserResponseDto register(@Valid @RequestBody UserRegistrationRequestDto request)
            throws RegistrationException {
        return userService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Log in to the application",
            description = "Authenticates a user and returns a JWT token upon successful login.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Login successful, JWT token returned"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid input data (e.g., empty password)"),
            @ApiResponse(responseCode = "401",
                    description = "Authentication failed (bad credentials)")
    })
    public UserLoginResponseDto login(@Valid @RequestBody UserLoginRequestDto userLoginRequestDto) {
        return authenticationService.authenticate(userLoginRequestDto);
    }
}

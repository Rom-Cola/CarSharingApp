package com.loievroman.carsharingapp.controller;

import com.loievroman.carsharingapp.dto.rental.CreateRentalRequestDto;
import com.loievroman.carsharingapp.dto.rental.RentalDto;
import com.loievroman.carsharingapp.model.User;
import com.loievroman.carsharingapp.service.RentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rentals")
public class RentalController {

    private final RentalService rentalService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseStatus(HttpStatus.CREATED)
    public RentalDto createRental(
            @RequestBody @Valid CreateRentalRequestDto requestDto,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        return rentalService.createRental(requestDto, currentUser);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Page<RentalDto> getRentals(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "true") boolean isActive,
            Authentication authentication,
            Pageable pageable
    ) {
        User currentUser = (User) authentication.getPrincipal();
        return rentalService.getRentalsByUserIdAndStatus(userId, currentUser, isActive, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public RentalDto getRentalById(@PathVariable Long id, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return rentalService.getRentalById(id, currentUser);
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasRole('MANAGER')")
    public RentalDto returnRental(@PathVariable Long id) {
        return rentalService.returnRental(id);
    }
}

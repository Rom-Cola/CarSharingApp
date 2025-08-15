package com.loievroman.carsharingapp.controller;

import com.loievroman.carsharingapp.dto.rental.CreateRentalRequestDto;
import com.loievroman.carsharingapp.dto.rental.RentalDto;
import com.loievroman.carsharingapp.service.RentalService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public RentalDto createRental(@RequestBody @Valid CreateRentalRequestDto requestDto) {
        return rentalService.createRental(requestDto);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<RentalDto> getRentals(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "true") boolean isActive) {
        return rentalService.getRentalsByUserIdAndStatus(userId, isActive);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public RentalDto getRentalById(@PathVariable Long id) {
        return rentalService.getRentalById(id);
    }

}

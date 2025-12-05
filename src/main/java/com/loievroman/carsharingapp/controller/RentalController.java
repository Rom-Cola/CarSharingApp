package com.loievroman.carsharingapp.controller;

import com.loievroman.carsharingapp.dto.rental.CreateRentalRequestDto;
import com.loievroman.carsharingapp.dto.rental.RentalDto;
import com.loievroman.carsharingapp.model.User;
import com.loievroman.carsharingapp.service.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Rental Management", description = "Endpoints for managing car rentals")
@RestController
@RequiredArgsConstructor
@RequestMapping("/rentals")
public class RentalController {

    private final RentalService rentalService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new rental", description = "Creates a new car "
            + "rental for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Rental created successfully"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid input data or no available cars"),
            @ApiResponse(responseCode = "404",
                    description = "Car with the specified ID not found")
    })
    public RentalDto createRental(
            @RequestBody @Valid CreateRentalRequestDto requestDto,
            @AuthenticationPrincipal User currentUser
    ) {
        return rentalService.createRental(requestDto, currentUser);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get my rentals",
            description = "Retrieves a paginated list of rentals "
                    + "for the currently authenticated user (works for Customers and Managers).")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved own rentals")
    public Page<RentalDto> getMyRentals(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "true") boolean isActive,
            Pageable pageable
    ) {
        return rentalService.findByUserIdAndStatus(currentUser.getId(), isActive, pageable);
    }

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get all or user-specific rentals (Admin)",
            description = "Retrieves a paginated list of all rentals. "
                    + "Can be filtered by a specific `userId` and `isActive` status.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved rentals")
    public Page<RentalDto> getAllRentals(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "true") boolean isActive,
            Pageable pageable
    ) {
        if (userId != null) {
            return rentalService.findByUserIdAndStatus(userId, isActive, pageable);
        }
        return rentalService.findAllByStatus(isActive, pageable);
    }

    @GetMapping("/my/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get my specific rental by ID",
            description = "Retrieves details for a specific rental belonging to the current user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successfully retrieved rental"),
            @ApiResponse(responseCode = "403",
                    description = "Access denied if you are not the owner"),
            @ApiResponse(responseCode = "404",
                    description = "Rental not found")
    })
    public RentalDto getMyRentalById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        return rentalService.findMyRentalById(id, currentUser.getId());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get any rental by ID (Admin)",
            description = "Retrieves details for any specific rental. Accessible only by managers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved rental"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Rental not found")
    })
    public RentalDto getRentalByIdAsManager(@PathVariable Long id) {
        return rentalService.findById(id);
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Return a car (Admin)",
            description = "Marks a rental as complete by setting the actual return date.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Car returned successfully"),
            @ApiResponse(responseCode = "404",
                    description = "Rental not found"),
            @ApiResponse(responseCode = "409",
                    description = "This rental has already been returned")
    })
    public RentalDto returnRental(@PathVariable Long id) {
        return rentalService.returnRental(id);
    }
}

package com.loievroman.carsharingapp.controller;

import com.loievroman.carsharingapp.dto.car.CarDto;
import com.loievroman.carsharingapp.dto.car.CreateCarRequestDto;
import com.loievroman.carsharingapp.service.CarService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Car Management", description = "Endpoints for managing the car inventory")
@RestController
@RequiredArgsConstructor
@RequestMapping("/cars")
public class CarController {

    private final CarService carService;

    @GetMapping("/{id}")
    @Operation(summary = "Get a car by ID",
            description = "Retrieves detailed information about a specific car.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the car"),
            @ApiResponse(responseCode = "404", description = "Car not found with the specified ID")
    })
    public CarDto findById(@PathVariable Long id) {
        return carService.findById(id);
    }

    @GetMapping
    @Operation(summary = "Get all cars",
            description = "Retrieves a paginated list of all available cars."
                    + " Supports sorting by any car field.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of cars")
    public Page<CarDto> findAll(Pageable pageable) {
        return carService.findAll(pageable);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new car (Admin)",
            description = "Adds a new car to the inventory. Accessible only by managers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Car created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public CarDto createCar(@Valid @RequestBody CreateCarRequestDto createCarRequestDto) {
        return carService.create(createCarRequestDto);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{id}")
    @Operation(summary = "Update a car (Admin)",
            description = "Updates the details of an existing car. Accessible only by managers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Car updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Car not found with the specified ID")
    })
    public CarDto updateCar(
            @PathVariable Long id,
            @Valid @RequestBody CreateCarRequestDto carRequestDto
    ) {
        return carService.update(id, carRequestDto);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a car (Admin)",
            description = "Deletes a car from the inventory (soft delete)."
                    + " Accessible only by managers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Car deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Car not found with the specified ID")
    })
    public void deleteById(@PathVariable Long id) {
        carService.delete(id);
    }
}

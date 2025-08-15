package com.loievroman.carsharingapp.controller;

import com.loievroman.carsharingapp.dto.car.CarDto;
import com.loievroman.carsharingapp.dto.car.CreateCarRequestDto;
import com.loievroman.carsharingapp.service.CarService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cars")
public class CarController {

    private final CarService carService;

    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @GetMapping("/{id}")
    public CarDto findById(@PathVariable Long id) {
        return carService.findById(id);
    }

    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @GetMapping
    public List<CarDto> findAll() {
        return carService.findAll();
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PostMapping
    public CarDto createCar(@RequestBody CreateCarRequestDto createCarRequestDto) {
        return carService.create(createCarRequestDto);
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PutMapping("/{id}")
    public CarDto updateCar(@PathVariable Long id, @RequestBody CreateCarRequestDto carRequestDto) {
        return carService.update(id, carRequestDto);
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        carService.delete(id);
    }
}

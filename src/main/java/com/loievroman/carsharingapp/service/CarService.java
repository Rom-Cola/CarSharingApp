package com.loievroman.carsharingapp.service;

import com.loievroman.carsharingapp.dto.car.CarDto;
import com.loievroman.carsharingapp.dto.car.CreateCarRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService {

    CarDto create(CreateCarRequestDto createCarRequestDto);

    CarDto findById(Long id);

    Page<CarDto> findAll(Pageable pageable);

    CarDto update(Long id, CreateCarRequestDto requestDto);

    void delete(Long id);
}

package com.loievroman.carsharingapp.service;

import com.loievroman.carsharingapp.dto.car.CarDto;
import com.loievroman.carsharingapp.dto.car.CreateCarRequestDto;
import java.util.List;

public interface CarService {

    CarDto create(CreateCarRequestDto createCarRequestDto);

    CarDto findById(Long id);

    List<CarDto> findAll();

    CarDto update(Long id, CreateCarRequestDto requestDto);

    void delete(Long id);
}

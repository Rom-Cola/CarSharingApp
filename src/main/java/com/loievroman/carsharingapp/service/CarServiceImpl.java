package com.loievroman.carsharingapp.service;

import com.loievroman.carsharingapp.dto.car.CarDto;
import com.loievroman.carsharingapp.dto.car.CreateCarRequestDto;
import com.loievroman.carsharingapp.exception.EntityNotFoundException;
import com.loievroman.carsharingapp.mapper.CarMapper;
import com.loievroman.carsharingapp.model.Car;
import com.loievroman.carsharingapp.repository.CarRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CarServiceImpl implements CarService {
    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @Override
    public CarDto create(CreateCarRequestDto createCarRequestDto) {
        Car model = carMapper.toModel(createCarRequestDto);
        return carMapper.toDto(carRepository.save(model));
    }

    @Override
    public CarDto findById(Long id) {
        Optional<Car> optionalCar = carRepository.findById(id);
        Car car = optionalCar
                .orElseThrow(() -> new EntityNotFoundException("Cannot find car with id=" + id));
        return carMapper.toDto(car);
    }

    @Override
    public Page<CarDto> findAll(Pageable pageable) {
        Page<Car> carPage = carRepository.findAll(pageable);
        return carPage.map(carMapper::toDto);
    }

    @Override
    public CarDto update(Long id, CreateCarRequestDto requestDto) {
        Optional<Car> optionalCar = carRepository.findById(id);
        Car foundedCar = optionalCar.orElseThrow(() -> new EntityNotFoundException(
                "Cannot find car to update with id=" + id
        ));
        Car updatedCar = carMapper.updateEntity(requestDto, foundedCar);
        carRepository.save(updatedCar);
        return carMapper.toDto(updatedCar);
    }

    @Override
    public void delete(Long id) {
        carRepository.deleteById(id);
    }
}

package com.loievroman.carsharingapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.loievroman.carsharingapp.dto.car.CarDto;
import com.loievroman.carsharingapp.dto.car.CreateCarRequestDto;
import com.loievroman.carsharingapp.mapper.CarMapper;
import com.loievroman.carsharingapp.model.Car;
import com.loievroman.carsharingapp.model.CarType;
import com.loievroman.carsharingapp.repository.CarRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarMapper carMapper;

    @InjectMocks
    private CarServiceImpl carService;

    @Test
    @DisplayName("Save a new car")
    void save_ValidRequest_ReturnsCarDto() {
        // given
        CreateCarRequestDto requestDto = new CreateCarRequestDto().setModel("Model S")
                                                                  .setBrand("Tesla")
                                                                  .setType(CarType.SEDAN)
                                                                  .setInventory(5)
                                                                  .setDailyFee(BigDecimal
                                                                          .valueOf(200.00));

        Car car = new Car();
        car.setId(1L);
        car.setModel(requestDto.getModel());

        CarDto carDto = new CarDto();
        carDto.setId(1L);
        carDto.setModel(requestDto.getModel());

        when(carMapper.toModel(requestDto)).thenReturn(car);
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toDto(car)).thenReturn(carDto);

        // when
        CarDto result = carService.create(requestDto);

        // then
        assertNotNull(result);
        assertEquals(carDto.getId(), result.getId());
    }

    @Test
    @DisplayName("Get all cars")
    void findAll_ReturnsPageOfCarDtos() {
        // given
        Car car = new Car();
        car.setId(1L);
        CarDto carDto = new CarDto();
        carDto.setId(1L);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Car> carPage = new PageImpl<>(List.of(car), pageable, 1);

        when(carRepository.findAll(pageable)).thenReturn(carPage);
        when(carMapper.toDto(car)).thenReturn(carDto);

        // when
        Page<CarDto> result = carService.findAll(pageable);

        // then
        assertEquals(1, result.getTotalElements());
        assertEquals(carDto.getId(), result.getContent().get(0).getId());
    }

    @Test
    @DisplayName("Get car by ID")
    void findById_ValidId_ReturnsCarDto() {
        // given
        Car car = new Car();
        car.setId(1L);
        CarDto carDto = new CarDto();
        carDto.setId(1L);

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(carMapper.toDto(car)).thenReturn(carDto);

        // when
        CarDto result = carService.findById(1L);

        // then
        assertNotNull(result);
        assertEquals(carDto.getId(), result.getId());
    }

    @Test
    @DisplayName("Update a car")
    void update_ValidRequest_ReturnsUpdatedCarDto() {
        // given
        Car car = new Car();
        car.setId(1L);
        car.setModel("Model S");

        Car updatedCar = new Car();
        updatedCar.setId(1L);
        updatedCar.setModel("Model X");

        CarDto updatedCarDto = new CarDto();
        updatedCarDto.setId(1L);
        updatedCarDto.setModel("Model X");

        CreateCarRequestDto requestDto = new CreateCarRequestDto().setModel("Model X");

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(carMapper.updateEntity(requestDto, car)).thenReturn(updatedCar);
        when(carRepository.save(updatedCar)).thenReturn(updatedCar);
        when(carMapper.toDto(updatedCar)).thenReturn(updatedCarDto);

        // when
        CarDto result = carService.update(1L, requestDto);

        // then
        assertNotNull(result);
        assertEquals(updatedCarDto.getModel(), result.getModel());
    }

    @Test
    @DisplayName("Delete a car")
    void delete_ValidId_CallsRepositoryDelete() {
        // given
        when(carRepository.existsById(1L)).thenReturn(true);

        // when
        carService.delete(1L);

        // then
        verify(carRepository).deleteById(1L);
    }
}

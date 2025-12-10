package com.loievroman.carsharingapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.loievroman.carsharingapp.dto.rental.CreateRentalRequestDto;
import com.loievroman.carsharingapp.dto.rental.RentalDto;
import com.loievroman.carsharingapp.mapper.RentalMapper;
import com.loievroman.carsharingapp.model.Car;
import com.loievroman.carsharingapp.model.Rental;
import com.loievroman.carsharingapp.model.User;
import com.loievroman.carsharingapp.repository.CarRepository;
import com.loievroman.carsharingapp.repository.RentalRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private RentalMapper rentalMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RentalServiceImpl rentalService;

    @Test
    @DisplayName("Create a new rental")
    void createRental_ValidRequest_ReturnsRentalDto() {
        // given
        CreateRentalRequestDto requestDto = new CreateRentalRequestDto();
        requestDto.setCarId(1L);
        requestDto.setReturnDate(LocalDate.of(2023, 1, 20));

        User user = new User();
        user.setId(1L);

        Car car = new Car();
        car.setId(1L);
        car.setInventory(1);

        Rental rental = new Rental();
        rental.setId(1L);

        RentalDto rentalDto = new RentalDto();
        rentalDto.setId(1L);

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(rentalRepository.save(any(Rental.class))).thenReturn(rental);
        when(rentalMapper.toDto(rental)).thenReturn(rentalDto);

        // when
        RentalDto result = rentalService.createRental(requestDto, user);

        // then
        assertNotNull(result);
        assertEquals(rentalDto.getId(), result.getId());
        assertEquals(0, car.getInventory());
    }

    @Test
    @DisplayName("Set actual return date")
    void setActualReturnDate_ValidId_ReturnsRentalDto() {
        // given
        User user = new User();
        user.setId(1L);

        Rental rental = new Rental();
        rental.setId(1L);
        Car car = new Car();
        car.setId(1L);
        car.setInventory(0);
        rental.setCar(car);
        rental.setUser(user);

        RentalDto rentalDto = new RentalDto();
        rentalDto.setActualReturnDate(LocalDate.now());

        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));
        when(rentalRepository.save(rental)).thenReturn(rental);
        when(rentalMapper.toDto(rental)).thenReturn(rentalDto);

        // when
        RentalDto result = rentalService.returnRental(1L);

        // then
        assertNotNull(result.getActualReturnDate());
        assertEquals(1, car.getInventory());
    }
}

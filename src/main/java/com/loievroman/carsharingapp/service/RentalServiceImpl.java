package com.loievroman.carsharingapp.service;

import com.loievroman.carsharingapp.dto.rental.CreateRentalRequestDto;
import com.loievroman.carsharingapp.dto.rental.RentalDto;
import com.loievroman.carsharingapp.exception.EntityNotFoundException;
import com.loievroman.carsharingapp.exception.NoAvailableCarsException;
import com.loievroman.carsharingapp.exception.RentalAlreadyReturnedException;
import com.loievroman.carsharingapp.mapper.RentalMapper;
import com.loievroman.carsharingapp.model.Car;
import com.loievroman.carsharingapp.model.Rental;
import com.loievroman.carsharingapp.model.User;
import com.loievroman.carsharingapp.repository.CarRepository;
import com.loievroman.carsharingapp.repository.RentalRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {
    private final CarRepository carRepository;
    private final RentalRepository rentalRepository;
    private final RentalMapper rentalMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public RentalDto createRental(CreateRentalRequestDto rentalRequestDto, User user) {
        Car car = carRepository.findById(rentalRequestDto.getCarId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Car not found with id: " + rentalRequestDto.getCarId()
                ));
        if (car.getInventory() <= 0) {
            throw new NoAvailableCarsException("No available cars with id: " + car.getId());
        }

        car.setInventory(car.getInventory() - 1);
        carRepository.save(car);

        Rental rental = new Rental();
        rental.setCar(car);

        rental.setUser(user);
        rental.setRentalDate(LocalDate.now());
        rental.setReturnDate(rentalRequestDto.getReturnDate());
        Rental savedRental = rentalRepository.save(rental);

        notificationService.sendNewRentalNotification(savedRental);

        return rentalMapper.toDto(savedRental);
    }

    @Override
    public Page<RentalDto> findByUserIdAndStatus(Long userId, boolean isActive, Pageable pageable) {
        Page<Rental> rentals;
        if (isActive) {
            rentals = rentalRepository.findByUserIdAndActualReturnDateIsNull(userId, pageable);
        } else {
            rentals = rentalRepository.findByUserIdAndActualReturnDateIsNotNull(userId, pageable);
        }
        return rentals.map(rentalMapper::toDto);
    }

    @Override
    public Page<RentalDto> findAllByStatus(boolean isActive, Pageable pageable) {
        Page<Rental> rentals;
        if (isActive) {
            rentals = rentalRepository.findByActualReturnDateIsNull(pageable);
        } else {
            rentals = rentalRepository.findByActualReturnDateIsNotNull(pageable);
        }
        return rentals.map(rentalMapper::toDto);
    }

    @Override
    public RentalDto findById(Long id) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rental not found with id: " + id));

        return rentalMapper.toDto(rental);
    }

    public RentalDto findMyRentalById(Long rentalId, Long userId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new EntityNotFoundException("Rental not found with id: "
                        + rentalId));

        if (!rental.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to view this rental.");
        }

        return rentalMapper.toDto(rental);
    }

    @Override
    @Transactional
    public RentalDto returnRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cannot return rental. Rental not found with id: " + rentalId
                ));

        if (rental.getActualReturnDate() != null) {
            throw new RentalAlreadyReturnedException(
                    "Rental with id " + rentalId + " has already been returned."
            );
        }

        rental.setActualReturnDate(LocalDate.now());
        Rental updatedRental = rentalRepository.save(rental);

        Car carToReturn = updatedRental.getCar();
        carToReturn.setInventory(carToReturn.getInventory() + 1);
        carRepository.save(carToReturn);

        notificationService.sendRentalReturnedNotification(updatedRental);

        return rentalMapper.toDto(updatedRental);
    }
}

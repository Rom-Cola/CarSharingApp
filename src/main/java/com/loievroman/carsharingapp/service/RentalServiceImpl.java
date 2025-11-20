package com.loievroman.carsharingapp.service;

import com.loievroman.carsharingapp.dto.rental.CreateRentalRequestDto;
import com.loievroman.carsharingapp.dto.rental.RentalDto;
import com.loievroman.carsharingapp.exception.EntityNotFoundException;
import com.loievroman.carsharingapp.exception.NoAvailableCarsException;
import com.loievroman.carsharingapp.exception.RentalAlreadyReturnedException;
import com.loievroman.carsharingapp.mapper.RentalMapper;
import com.loievroman.carsharingapp.model.Car;
import com.loievroman.carsharingapp.model.Rental;
import com.loievroman.carsharingapp.model.Role;
import com.loievroman.carsharingapp.model.User;
import com.loievroman.carsharingapp.repository.CarRepository;
import com.loievroman.carsharingapp.repository.RentalRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

        notificationService.sendNewRentalNotification(rental);

        return rentalMapper.toDto(savedRental);
    }

    @Override
    public List<RentalDto> getRentalsByUserIdAndStatus(
            Long userId,
            User currentUser,
            boolean isActive
    ) {

        if (currentUser.getRoles().stream().anyMatch(
                r -> r.getName().equals(Role.RoleName.MANAGER
                ))) {
            return findRentalsForManager(userId, isActive);
        } else {
            return findRentalsForCustomer(currentUser.getId(), isActive);
        }
    }

    private List<RentalDto> findRentalsForManager(Long userId, boolean isActive) {
        List<Rental> rentals;
        if (userId != null) {
            return findRentalsForCustomer(userId, isActive);
        } else {
            if (isActive) {
                rentals = rentalRepository.findByActualReturnDateIsNull();
            } else {
                rentals = rentalRepository.findByActualReturnDateIsNotNull();
            }
        }

        return rentals.stream()
                .map(rentalMapper::toDto)
                .toList();
    }

    private List<RentalDto> findRentalsForCustomer(Long customerId, boolean isActive) {
        List<Rental> rentals;

        if (isActive) {
            rentals = rentalRepository.findByUserIdAndActualReturnDateIsNull(customerId);
        } else {
            rentals = rentalRepository.findByUserIdAndActualReturnDateIsNotNull(customerId);
        }

        return rentals.stream()
                .map(rentalMapper::toDto)
                .toList();
    }

    @Override
    public RentalDto getRentalById(Long id, User currentUser) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rental not found with id: " + id));

        boolean isManager = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_MANAGER"));

        if (!isManager && !rental.getUser().getId().equals(currentUser.getId())) {
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

        return rentalMapper.toDto(updatedRental);
    }
}

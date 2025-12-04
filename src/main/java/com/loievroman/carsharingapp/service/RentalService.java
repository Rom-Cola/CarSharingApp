package com.loievroman.carsharingapp.service;

import com.loievroman.carsharingapp.dto.rental.CreateRentalRequestDto;
import com.loievroman.carsharingapp.dto.rental.RentalDto;
import com.loievroman.carsharingapp.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RentalService {

    RentalDto createRental(CreateRentalRequestDto rentalRequestDto, User user);

    RentalDto findById(Long id);

    RentalDto findMyRentalById(Long rentalId, Long userId);

    RentalDto returnRental(Long rentalId);

    Page<RentalDto> findByUserIdAndStatus(Long userId, boolean isActive, Pageable pageable);

    Page<RentalDto> findAllByStatus(boolean isActive, Pageable pageable);
}

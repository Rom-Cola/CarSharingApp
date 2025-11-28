package com.loievroman.carsharingapp.service;

import com.loievroman.carsharingapp.dto.rental.CreateRentalRequestDto;
import com.loievroman.carsharingapp.dto.rental.RentalDto;
import com.loievroman.carsharingapp.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RentalService {

    RentalDto createRental(CreateRentalRequestDto rentalRequestDto, User user);

    Page<RentalDto> getRentalsByUserIdAndStatus(Long userId, User currentUser, boolean isActive,
                                                Pageable pageable);

    RentalDto getRentalById(Long id, User currentUser);

    RentalDto returnRental(Long rentalId);
}

package com.loievroman.carsharingapp.service;

import com.loievroman.carsharingapp.dto.rental.CreateRentalRequestDto;
import com.loievroman.carsharingapp.dto.rental.RentalDto;
import com.loievroman.carsharingapp.model.User;
import java.util.List;

public interface RentalService {

    RentalDto createRental(CreateRentalRequestDto rentalRequestDto, User user);

    List<RentalDto> getRentalsByUserIdAndStatus(Long userId, User currentUser, boolean isActive);

    RentalDto getRentalById(Long id, User currentUser);

    RentalDto returnRental(Long rentalId);
}

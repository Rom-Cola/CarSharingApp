package com.loievroman.carsharingapp.service;

import com.loievroman.carsharingapp.dto.rental.CreateRentalRequestDto;
import com.loievroman.carsharingapp.dto.rental.RentalDto;
import java.util.List;

public interface RentalService {

    RentalDto createRental(CreateRentalRequestDto rentalRequestDto);

    List<RentalDto> getRentalsByUserIdAndStatus(Long userId, boolean isActive);

    RentalDto getRentalById(Long id);

    RentalDto returnRental(Long rentalId);
}

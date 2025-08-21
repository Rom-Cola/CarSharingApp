package com.loievroman.carsharingapp.dto.rental;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RentalDto {
    private Long id;
    private LocalDate rentalDate;
    private LocalDate returnDate;
    private Long carId;
    private Long userId;
}

package com.loievroman.carsharingapp.dto.rental;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class CreateRentalRequestDto {
    @NotNull
    private Long carId;

    @NotNull
    @Future
    private LocalDate returnDate;
}

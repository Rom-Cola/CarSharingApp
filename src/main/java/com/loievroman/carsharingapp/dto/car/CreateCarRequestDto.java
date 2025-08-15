package com.loievroman.carsharingapp.dto.car;

import com.loievroman.carsharingapp.model.CarType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateCarRequestDto {
    @NotBlank
    private String model;
    @NotBlank
    private String brand;
    @NotNull
    private CarType type;
    @NotNull
    @Min(0)
    private int inventory;
    @NotNull
    @Min(0)
    private BigDecimal dailyFee;
}

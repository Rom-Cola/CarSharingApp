package com.loievroman.carsharingapp.dto.car;

import com.loievroman.carsharingapp.model.CarType;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(
            description = "The model of the car",
            example = "A6",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String model;

    @NotBlank
    @Schema(
            description = "The brand of the car",
            example = "Audi",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String brand;

    @Schema(
            description = "The type of the car body. "
                    + "Available values: SEDAN, SUV, HATCHBACK, UNIVERSAL",
            example = "SEDAN",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    private CarType type;

    @NotNull
    @Min(0)
    @Schema(
            description = "Number of available units for this car model",
            example = "5",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private int inventory;

    @NotNull
    @Min(0)
    @Schema(
            description = "Daily rental fee in USD",
            example = "75.50",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private BigDecimal dailyFee;
}

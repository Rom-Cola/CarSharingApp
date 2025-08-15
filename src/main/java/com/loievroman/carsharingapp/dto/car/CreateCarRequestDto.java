package com.loievroman.carsharingapp.dto.car;

import com.loievroman.carsharingapp.model.CarType;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateCarRequestDto {
    private String model;
    private String brand;
    private CarType type;
    private int inventory;
    private BigDecimal dailyFee;
}

package com.loievroman.carsharingapp.dto.car;

import com.loievroman.carsharingapp.model.CarType;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CarDto {

    private Long id;
    private String model;
    private String brand;
    private String carType;
    private int inventory;
    private BigDecimal dailyFee;
}

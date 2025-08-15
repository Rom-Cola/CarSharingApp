package com.loievroman.carsharingapp.mapper;

import com.loievroman.carsharingapp.config.MapperConfig;
import com.loievroman.carsharingapp.dto.rental.RentalDto;
import com.loievroman.carsharingapp.model.Rental;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface RentalMapper {
    @Mapping(source = "car.id", target = "carId")
    @Mapping(source = "user.id", target = "userId")
    RentalDto toDto(Rental rental);

    Rental toModel(RentalDto rentalDto);
}

package com.loievroman.carsharingapp.mapper;

import com.loievroman.carsharingapp.config.MapperConfig;
import com.loievroman.carsharingapp.dto.car.CarDto;
import com.loievroman.carsharingapp.dto.car.CreateCarRequestDto;
import com.loievroman.carsharingapp.model.Car;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface CarMapper {

    @Mapping(source = "type", target = "carType")
    CarDto toDto(Car car);

    Car toModel(CreateCarRequestDto createCarRequestDto);

    Car updateEntity(CreateCarRequestDto createCarRequestDto, @MappingTarget Car car);

}

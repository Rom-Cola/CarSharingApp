package com.loievroman.carsharingapp.mapper;

import com.loievroman.carsharingapp.config.MapperConfig;
import com.loievroman.carsharingapp.dto.payment.CreatePaymentRequestDto;
import com.loievroman.carsharingapp.dto.payment.PaymentDto;
import com.loievroman.carsharingapp.dto.payment.PaymentResponseDto;
import com.loievroman.carsharingapp.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {

    @Mapping(source = "type", target = "paymentType")
    @Mapping(source = "status", target = "paymentStatus")
    PaymentDto toDto(Payment payment);

    Payment toModel(CreatePaymentRequestDto paymentDto);

    PaymentResponseDto toResponseDto(Payment payment);
}

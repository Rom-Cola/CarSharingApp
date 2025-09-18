package com.loievroman.carsharingapp.dto.payment;

import com.loievroman.carsharingapp.model.PaymentType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreatePaymentRequestDto {

    @NotNull
    private Long rentalId;

    @NotNull
    private PaymentType type;
}

package com.loievroman.carsharingapp.dto.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentResponseDto {
    private String sessionUrl;
    private String sessionId;
}

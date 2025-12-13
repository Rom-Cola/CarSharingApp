package com.loievroman.carsharingapp.dto.payment;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentDto {
    private Long id;
    private String paymentStatus;
    private String paymentType;
    private Long rentalId;
    private String sessionUrl;
    private String sessionId;
    private BigDecimal amountToPay;
}

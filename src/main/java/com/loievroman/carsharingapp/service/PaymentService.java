package com.loievroman.carsharingapp.service;

import com.loievroman.carsharingapp.dto.payment.CreatePaymentRequestDto;
import com.loievroman.carsharingapp.dto.payment.PaymentDto;
import com.loievroman.carsharingapp.dto.payment.PaymentResponseDto;
import java.util.List;

public interface PaymentService {
    List<PaymentDto> getPayments(Long userId);

    // TODO: handleSuccess(), handleCancel()
    PaymentResponseDto createPaymentSession(CreatePaymentRequestDto requestDto);
}

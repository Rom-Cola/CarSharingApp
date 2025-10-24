package com.loievroman.carsharingapp.service;

import com.loievroman.carsharingapp.dto.payment.CreatePaymentRequestDto;
import com.loievroman.carsharingapp.dto.payment.PaymentDto;
import com.loievroman.carsharingapp.dto.payment.PaymentResponseDto;
import com.loievroman.carsharingapp.dto.payment.PaymentStatusResponseDto;
import java.util.List;

public interface PaymentService {
    List<PaymentDto> findByUserId(Long userId);

    PaymentStatusResponseDto handleSuccessfulPayment(String sessionId);

    PaymentResponseDto createPaymentSession(CreatePaymentRequestDto requestDto);

    PaymentStatusResponseDto handleCancelledPayment(String sessionId);

    List<PaymentDto> findAll();
}

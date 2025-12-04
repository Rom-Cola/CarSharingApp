package com.loievroman.carsharingapp.service;

import com.loievroman.carsharingapp.dto.payment.CreatePaymentRequestDto;
import com.loievroman.carsharingapp.dto.payment.PaymentDto;
import com.loievroman.carsharingapp.dto.payment.PaymentResponseDto;
import com.loievroman.carsharingapp.dto.payment.PaymentStatusResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.util.UriComponentsBuilder;

public interface PaymentService {
    Page<PaymentDto> findByUserId(Long userId, Pageable pageable);

    PaymentStatusResponseDto handleSuccessfulPayment(String sessionId);

    PaymentResponseDto createPaymentSession(CreatePaymentRequestDto requestDto,
                                            UriComponentsBuilder uriComponentsBuilder);

    PaymentStatusResponseDto handleCancelledPayment(String sessionId);

    Page<PaymentDto> findAll(Pageable pageable);
}

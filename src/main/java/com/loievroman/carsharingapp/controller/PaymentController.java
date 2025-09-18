package com.loievroman.carsharingapp.controller;

import com.loievroman.carsharingapp.dto.payment.CreatePaymentRequestDto;
import com.loievroman.carsharingapp.dto.payment.PaymentDto;
import com.loievroman.carsharingapp.dto.payment.PaymentResponseDto;
import com.loievroman.carsharingapp.service.PaymentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public List<PaymentDto> getPayments(@RequestParam(required = false) Long userId) {

        return paymentService.getPayments(userId);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public PaymentResponseDto createPaymentSession(
            @Valid @RequestBody CreatePaymentRequestDto requestDto
    ) {
        return paymentService.createPaymentSession(requestDto);
    }

    @GetMapping("/success")
    public String handleSuccessfulPayment() {
        // ToDo: finish endpoint implementation
        return "Payment was successful! (This endpoint is not fully implemented yet).";
    }

    @GetMapping("/cancel")
    public String handleCancelledPayment() {
        // ToDo: finish endpoint implementation
        return "Payment was cancelled. You can try again later.";
    }

}

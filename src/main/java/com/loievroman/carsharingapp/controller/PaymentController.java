package com.loievroman.carsharingapp.controller;

import com.loievroman.carsharingapp.dto.payment.CreatePaymentRequestDto;
import com.loievroman.carsharingapp.dto.payment.PaymentDto;
import com.loievroman.carsharingapp.dto.payment.PaymentResponseDto;
import com.loievroman.carsharingapp.dto.payment.PaymentStatusResponseDto;
import com.loievroman.carsharingapp.model.User;
import com.loievroman.carsharingapp.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
    public Page<PaymentDto> getPayments(
            @RequestParam(required = false) Long userId,
            Authentication authentication,
            Pageable pageable
    ) {
        User currentUser = (User) authentication.getPrincipal();

        boolean isManager = currentUser.getAuthorities().stream()
                .anyMatch(
                        auth -> auth
                                .getAuthority().equals("ROLE_MANAGER")
                );
        if (isManager) {
            if (userId != null) {
                return paymentService.findByUserId(userId, pageable);
            } else {
                return paymentService.findAll(pageable);
            }
        }
        return paymentService.findByUserId(currentUser.getId(), pageable);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public PaymentResponseDto createPaymentSession(
            @Valid @RequestBody CreatePaymentRequestDto requestDto
    ) {
        return paymentService.createPaymentSession(requestDto);
    }

    @GetMapping("/success")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public PaymentStatusResponseDto handleSuccessfulPayment(
            @RequestParam("session_id") String sessionId) {
        return paymentService.handleSuccessfulPayment(sessionId);
    }

    @GetMapping("/cancel")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public PaymentStatusResponseDto handleCancelledPayment(
            @RequestParam("session_id") String sessionId) {
        return paymentService.handleCancelledPayment(sessionId);
    }

}

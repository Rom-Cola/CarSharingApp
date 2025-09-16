package com.loievroman.carsharingapp.service;

import com.loievroman.carsharingapp.dto.payment.PaymentDto;
import com.loievroman.carsharingapp.mapper.PaymentMapper;
import com.loievroman.carsharingapp.repository.PaymentRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public List<PaymentDto> getPayments(Long userId) {
        // TODO: finish implementation
        return List.of();
    }

    public Session createTestPaymentSession() throws StripeException {
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(
                        "http://localhost:8080/payments/success?session_id={CHECKOUT_SESSION_ID}"
                )
                .setCancelUrl("http://localhost:8080/payments/cancel")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount(5000L)
                                                .setProductData(
                                                        SessionCreateParams
                                                                .LineItem
                                                                .PriceData
                                                                .ProductData.builder()
                                                                .setName("Test Rental: Audi A6")
                                                                .build()
                                                ).build()
                                )
                                .setQuantity(1L)
                                .build()
                )
                .build();
        return Session.create(params);
    }
}

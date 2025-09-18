package com.loievroman.carsharingapp.service;

import com.loievroman.carsharingapp.dto.payment.CreatePaymentRequestDto;
import com.loievroman.carsharingapp.dto.payment.PaymentDto;
import com.loievroman.carsharingapp.dto.payment.PaymentResponseDto;
import com.loievroman.carsharingapp.exception.EntityNotFoundException;
import com.loievroman.carsharingapp.exception.NoFineRequiredException;
import com.loievroman.carsharingapp.exception.PaymentAlreadyPaidException;
import com.loievroman.carsharingapp.exception.PaymentException;
import com.loievroman.carsharingapp.mapper.PaymentMapper;
import com.loievroman.carsharingapp.model.Payment;
import com.loievroman.carsharingapp.model.PaymentStatus;
import com.loievroman.carsharingapp.model.PaymentType;
import com.loievroman.carsharingapp.model.Rental;
import com.loievroman.carsharingapp.repository.PaymentRepository;
import com.loievroman.carsharingapp.repository.RentalRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private static final String SUCCESS_URL = "http://localhost:8080/payments/success";
    private static final String CANCEL_URL = "http://localhost:8080/payments/cancel";
    private static final BigDecimal FINE_MULTIPLIER = new BigDecimal("1.5");
    private final RentalRepository rentalRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public List<PaymentDto> getPayments(Long userId) {
        // TODO: finish implementation
        return List.of();
    }

    @Override
    @Transactional
    public PaymentResponseDto createPaymentSession(CreatePaymentRequestDto requestDto) {
        Optional<Payment> existingPayment = paymentRepository.findByRentalIdAndType(
                requestDto.getRentalId(),
                requestDto.getType()
        );

        if (existingPayment.isPresent()
                && existingPayment.get().getStatus() == PaymentStatus.PENDING) {
            return buildResponseDto(existingPayment.get());
        }

        if (existingPayment.isPresent()
                && existingPayment.get().getStatus() == PaymentStatus.PAID) {
            throw new PaymentAlreadyPaidException("A payment of type " + requestDto.getType()
                    + " for this rental has already been paid.");
        }

        Rental rental = rentalRepository.findById(requestDto.getRentalId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Rental not found for id: " + requestDto.getRentalId()
                ));

        BigDecimal amountToPay = calculateAmount(rental, requestDto.getType());

        if (requestDto.getType() == PaymentType.FINE
                && amountToPay.compareTo(BigDecimal.ZERO) == 0) {
            throw new NoFineRequiredException(
                    "No fine is required for this rental as it was returned on time or earlier."
            );
        }

        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.PENDING);
        payment.setType(requestDto.getType());
        payment.setRental(rental);
        payment.setAmountToPay(amountToPay);

        Payment savedPayment = paymentRepository.save(payment);

        try {
            SessionCreateParams params = buildSessionParams(savedPayment);
            Session session = Session.create(params);

            savedPayment.setSessionId(session.getId());
            savedPayment.setSessionUrl(session.getUrl());
            paymentRepository.save(savedPayment);

            PaymentResponseDto responseDto = new PaymentResponseDto();
            responseDto.setSessionUrl(session.getUrl());
            responseDto.setSessionId(session.getId());
            responseDto.setStatus(PaymentStatus.PENDING.toString());

            return responseDto;
        } catch (StripeException e) {
            throw new PaymentException("Can't create Stripe payment session");
        }
    }

    private BigDecimal calculateAmount(Rental rental, PaymentType paymentType) {
        if (paymentType == PaymentType.PAYMENT) {
            return calculateRegularPaymentAmount(rental);
        }

        if (paymentType == PaymentType.FINE) {
            return calculateFineAmount(rental);
        }

        throw new PaymentException("Unsupported payment type: " + paymentType);
    }

    private BigDecimal calculateRegularPaymentAmount(Rental rental) {
        BigDecimal dailyFee = rental.getCar().getDailyFee();
        long rentalDays = ChronoUnit.DAYS.between(
                rental.getRentalDate(), rental.getReturnDate()
        );

        long daysToPay = Math.max(1, rentalDays);

        return dailyFee.multiply(BigDecimal.valueOf(daysToPay));
    }

    private BigDecimal calculateFineAmount(Rental rental) {
        if (rental.getActualReturnDate() == null) {
            throw new IllegalStateException(
                    "Cannot calculate fine for an active rental. "
                    + "The car must be returned first.");
        }

        BigDecimal dailyFee = rental.getCar().getDailyFee();
        long overdueDays = ChronoUnit.DAYS.between(rental.getReturnDate(),
                rental.getActualReturnDate());

        if (overdueDays <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal baseFine = dailyFee.multiply(BigDecimal.valueOf(overdueDays));
        return baseFine.multiply(FINE_MULTIPLIER);
    }

    private PaymentResponseDto buildResponseDto(Payment payment) {
        PaymentResponseDto responseDto = new PaymentResponseDto();
        responseDto.setSessionUrl(payment.getSessionUrl());
        responseDto.setSessionId(payment.getSessionId());
        responseDto.setStatus(payment.getStatus().name());
        return responseDto;
    }

    private SessionCreateParams buildSessionParams(Payment payment) {
        String productName;
        if (payment.getType() == PaymentType.PAYMENT) {
            productName = String.format("Rental of %s %s",
                    payment.getRental().getCar().getBrand(),
                    payment.getRental().getCar().getModel());
        } else if (payment.getType() == PaymentType.FINE) {
            productName = String.format("Fine for overdue rental of %s %s",
                    payment.getRental().getCar().getBrand(),
                    payment.getRental().getCar().getModel());
        } else {
            throw new PaymentException("Cannot create session with payment type ="
                    + payment.getType());
        }

        long amountInCents = payment.getAmountToPay().multiply(BigDecimal.valueOf(100)).longValue();

        return SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(SUCCESS_URL + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(CANCEL_URL)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams
                                                                .LineItem
                                                                .PriceData
                                                                .ProductData.builder()
                                                                .setName(productName)
                                                                .build()
                                                ).build()
                                )
                                .setQuantity(1L)
                                .build()
                )
                .build();
    }
}

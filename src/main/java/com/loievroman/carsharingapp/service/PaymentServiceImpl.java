package com.loievroman.carsharingapp.service;

import com.loievroman.carsharingapp.dto.payment.CreatePaymentRequestDto;
import com.loievroman.carsharingapp.dto.payment.PaymentDto;
import com.loievroman.carsharingapp.dto.payment.PaymentResponseDto;
import com.loievroman.carsharingapp.dto.payment.PaymentStatusResponseDto;
import com.loievroman.carsharingapp.exception.EntityNotFoundException;
import com.loievroman.carsharingapp.exception.FineCalculationForActiveRentalException;
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
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private static final BigDecimal FINE_MULTIPLIER = new BigDecimal("1.5");
    private static final String RESPONSE_SUCCESS_STATUS = "SUCCESS";
    private static final String RESPONSE_PENDING_STATUS = "PENDING";
    private static final String RESPONSE_CANCELLED_STATUS = "CANCELLED";
    private final RentalRepository rentalRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final NotificationService telegramNotificationService;

    @Override
    public Page<PaymentDto> findByUserId(Long userId, Pageable pageable) {
        return paymentRepository.findByUserId(userId, pageable)
                .map(paymentMapper::toDto);
    }

    @Override
    @Transactional
    public PaymentStatusResponseDto handleSuccessfulPayment(String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);

            String paymentStatusFromStripe = session.getPaymentStatus();

            PaymentStatusResponseDto response = new PaymentStatusResponseDto();

            if (paymentStatusFromStripe.equalsIgnoreCase("paid")) {
                Payment payment = paymentRepository.findBySessionId(sessionId)
                        .orElseThrow(() -> new EntityNotFoundException("Payment not found "
                                + " for session id: " + sessionId));

                if (payment.getStatus() == PaymentStatus.PAID) {
                    response.setStatus(RESPONSE_SUCCESS_STATUS);
                    response.setMessage("This payment has already been successfully processed.");
                    return response;
                }

                payment.setStatus(PaymentStatus.PAID);
                paymentRepository.save(payment);

                telegramNotificationService.sendPaymentConfirmedNotification(payment);

                response.setStatus(RESPONSE_SUCCESS_STATUS);
                response.setMessage("Your payment was processed successfully!");
                return response;
            } else {

                response.setStatus(RESPONSE_PENDING_STATUS);
                response.setMessage(
                        "Payment is not confirmed yet. Status: " + paymentStatusFromStripe);

                return response;
            }

        } catch (StripeException e) {
            throw new PaymentException("Error retrieving session from Stripe: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public PaymentResponseDto createPaymentSession(CreatePaymentRequestDto requestDto,
                                                   UriComponentsBuilder uriComponentsBuilder) {
        Optional<Payment> existingPayment = paymentRepository.findByRentalIdAndType(
                requestDto.getRentalId(),
                requestDto.getType()
        );

        if (existingPayment.isPresent()
                && existingPayment.get().getStatus() == PaymentStatus.PENDING) {
            return paymentMapper.toResponseDto(existingPayment.get());
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
            SessionCreateParams params = buildSessionParams(savedPayment, uriComponentsBuilder);
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

    @Override
    public PaymentStatusResponseDto handleCancelledPayment(String sessionId) {
        PaymentStatusResponseDto response = new PaymentStatusResponseDto();
        response.setStatus(RESPONSE_CANCELLED_STATUS);
        response.setMessage(
                "Payment was cancelled. "
                        + "You can try to pay again from your profile. "
                        + "The session is available for 24 hours.");
        return response;
    }

    @Override
    public Page<PaymentDto> findAll(Pageable pageable) {
        return paymentRepository.findAll(pageable)
                .map(paymentMapper::toDto);
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
            throw new FineCalculationForActiveRentalException(
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

    private SessionCreateParams buildSessionParams(Payment payment,
                                                   UriComponentsBuilder baseUriBuilder) {
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

        String successUrl = baseUriBuilder.cloneBuilder()
                .replacePath("/payments/success")
                .replaceQueryParam("session_id", "{CHECKOUT_SESSION_ID}")
                .build()
                .toUriString();

        String cancelUrl = baseUriBuilder.cloneBuilder()
                .replacePath("/payments/cancel")
                .replaceQueryParam("session_id", "{CHECKOUT_SESSION_ID}")
                .build()
                .toUriString();

        return SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
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

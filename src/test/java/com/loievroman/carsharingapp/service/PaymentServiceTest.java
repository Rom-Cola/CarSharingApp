package com.loievroman.carsharingapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.loievroman.carsharingapp.dto.payment.CreatePaymentRequestDto;
import com.loievroman.carsharingapp.dto.payment.PaymentDto;
import com.loievroman.carsharingapp.dto.payment.PaymentResponseDto;
import com.loievroman.carsharingapp.dto.payment.PaymentStatusResponseDto;
import com.loievroman.carsharingapp.exception.NoFineRequiredException;
import com.loievroman.carsharingapp.exception.PaymentAlreadyPaidException;
import com.loievroman.carsharingapp.mapper.PaymentMapper;
import com.loievroman.carsharingapp.model.Car;
import com.loievroman.carsharingapp.model.Payment;
import com.loievroman.carsharingapp.model.PaymentStatus;
import com.loievroman.carsharingapp.model.PaymentType;
import com.loievroman.carsharingapp.model.Rental;
import com.loievroman.carsharingapp.model.User;
import com.loievroman.carsharingapp.repository.PaymentRepository;
import com.loievroman.carsharingapp.repository.RentalRepository;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.util.UriComponentsBuilder;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    @DisplayName("Create Payment Session - Success - Creates New Session")
    void createPaymentSession_ValidRequest_CreatesNewSession() {
        // given
        Long rentalId = 1L;
        CreatePaymentRequestDto requestDto = new CreatePaymentRequestDto();
        requestDto.setRentalId(rentalId);
        requestDto.setType(PaymentType.PAYMENT);

        Car car = new Car();
        car.setDailyFee(BigDecimal.valueOf(100.00));
        car.setBrand("Tesla");
        car.setModel("S");

        Rental rental = new Rental();
        rental.setId(rentalId);
        rental.setCar(car);
        rental.setRentalDate(LocalDate.now().minusDays(5));
        rental.setReturnDate(LocalDate.now());

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmountToPay(BigDecimal.valueOf(500.00));

        payment.setType(PaymentType.PAYMENT);
        payment.setRental(rental);

        Session sessionMock = mock(Session.class);
        when(sessionMock.getId()).thenReturn("sess_123");
        when(sessionMock.getUrl()).thenReturn("http://stripe.com/pay");

        when(paymentRepository.findByRentalIdAndType(rentalId, PaymentType.PAYMENT))
                .thenReturn(Optional.empty());
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("http://localhost");

        try (MockedStatic<Session> mockedStaticSession = mockStatic(Session.class)) {
            mockedStaticSession.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(sessionMock);

            // when
            PaymentResponseDto result = paymentService.createPaymentSession(requestDto, uriBuilder);

            // then
            assertNotNull(result);
            assertEquals("sess_123", result.getSessionId());
            assertEquals("http://stripe.com/pay", result.getSessionUrl());
            verify(paymentRepository, org.mockito.Mockito.times(2))
                    .save(any(Payment.class));
        }
    }

    @Test
    @DisplayName("Create Payment Session - Existing Pending Payment - Returns Existing")
    void createPaymentSession_ExistingPending_ReturnsExisting() {
        // given
        CreatePaymentRequestDto requestDto = new CreatePaymentRequestDto();
        requestDto.setRentalId(1L);
        requestDto.setType(PaymentType.PAYMENT);

        Payment existingPayment = new Payment();
        existingPayment.setStatus(PaymentStatus.PENDING);
        existingPayment.setSessionUrl("old_url");

        PaymentResponseDto expectedResponse = new PaymentResponseDto();
        expectedResponse.setSessionUrl("old_url");

        when(paymentRepository.findByRentalIdAndType(1L, PaymentType.PAYMENT))
                .thenReturn(Optional.of(existingPayment));
        when(paymentMapper.toResponseDto(existingPayment)).thenReturn(expectedResponse);

        // when
        PaymentResponseDto result = paymentService
                .createPaymentSession(requestDto, null);

        // then
        assertEquals("old_url", result.getSessionUrl());
        verify(rentalRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Create Payment Session - Already Paid - Throws Exception")
    void createPaymentSession_AlreadyPaid_ThrowsException() {
        // given
        CreatePaymentRequestDto requestDto = new CreatePaymentRequestDto();
        requestDto.setRentalId(1L);
        requestDto.setType(PaymentType.PAYMENT);

        Payment existingPayment = new Payment();
        existingPayment.setStatus(PaymentStatus.PAID);

        when(paymentRepository.findByRentalIdAndType(1L, PaymentType.PAYMENT))
                .thenReturn(Optional.of(existingPayment));

        // when & then
        assertThrows(PaymentAlreadyPaidException.class,
                () -> paymentService.createPaymentSession(requestDto, null));
    }

    @Test
    @DisplayName("Create Fine Session - No Fine Required - Throws Exception")
    void createPaymentSession_NoFineRequired_ThrowsException() {
        // given
        CreatePaymentRequestDto requestDto = new CreatePaymentRequestDto();
        requestDto.setRentalId(1L);
        requestDto.setType(PaymentType.FINE);

        Car car = new Car();
        car.setDailyFee(BigDecimal.TEN);

        Rental rental = new Rental();
        rental.setId(1L);
        rental.setCar(car);
        rental.setReturnDate(LocalDate.now());
        rental.setActualReturnDate(LocalDate.now()); // Returned on time

        when(paymentRepository.findByRentalIdAndType(1L, PaymentType.FINE))
                .thenReturn(Optional.empty());
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));

        // when & then
        assertThrows(NoFineRequiredException.class,
                () -> paymentService.createPaymentSession(requestDto, null));
    }

    @Test
    @DisplayName("Handle Successful Payment - Stripe Paid - Updates DB")
    void handleSuccessfulPayment_StripePaid_UpdatesDb() {
        // given
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");

        Car car = new Car();
        car.setBrand("Test");
        car.setModel("Car");

        Rental rental = new Rental();
        rental.setUser(user);
        rental.setCar(car);

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setRental(rental);
        payment.setType(PaymentType.PAYMENT);
        payment.setAmountToPay(BigDecimal.TEN);

        Session sessionMock = mock(Session.class);
        when(sessionMock.getPaymentStatus()).thenReturn("paid");

        String sessionId = "sess_success";

        when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.of(payment));

        try (MockedStatic<Session> mockedStaticSession = mockStatic(Session.class)) {
            mockedStaticSession.when(() -> Session.retrieve(sessionId)).thenReturn(sessionMock);

            // when
            PaymentStatusResponseDto result = paymentService.handleSuccessfulPayment(sessionId);

            // then
            assertEquals("SUCCESS", result.getStatus());
            assertEquals(PaymentStatus.PAID, payment.getStatus());
            verify(notificationService).sendPaymentConfirmedNotification(payment);
        }
    }

    @Test
    @DisplayName("Find By User ID - Returns Page of DTOs")
    void findByUserId_ValidUser_ReturnsPage() {
        // given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Payment payment = new Payment();
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setId(1L);

        Page<Payment> page = new PageImpl<>(List.of(payment));
        when(paymentRepository.findByUserId(userId, pageable)).thenReturn(page);
        when(paymentMapper.toDto(payment)).thenReturn(paymentDto);

        // when
        Page<PaymentDto> result = paymentService.findByUserId(userId, pageable);

        // then
        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getId());
    }
}

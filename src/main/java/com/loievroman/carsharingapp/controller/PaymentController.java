package com.loievroman.carsharingapp.controller;

import com.loievroman.carsharingapp.dto.payment.CreatePaymentRequestDto;
import com.loievroman.carsharingapp.dto.payment.PaymentDto;
import com.loievroman.carsharingapp.dto.payment.PaymentResponseDto;
import com.loievroman.carsharingapp.dto.payment.PaymentStatusResponseDto;
import com.loievroman.carsharingapp.exception.EntityNotFoundException;
import com.loievroman.carsharingapp.model.User;
import com.loievroman.carsharingapp.service.PaymentService;
import com.loievroman.carsharingapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.util.UriComponentsBuilder;

@Tag(name = "Payment Management",
        description = "Endpoints for managing payments and processing Stripe callbacks")
@RequiredArgsConstructor
@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(
            summary = "Get my payments (for Customers and Managers)",
            description = "Retrieves a paginated list of payments "
                    + "for the currently authenticated user. "
                    + "Since a MANAGER also has a CUSTOMER role,"
                    + " they can use this to see their personal payments."
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved own payments")
    public Page<PaymentDto> getMyPayments(Authentication authentication, Pageable pageable) {
        User currentUser = (User) authentication.getPrincipal();
        return paymentService.findByUserId(currentUser.getId(), pageable);
    }

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Get all or user-specific payments (for Managers)",
            description = "Retrieves a paginated list of all payments in the system. "
                    + "Can be filtered by a specific `userId`."
                    + " Accessible only by users with the MANAGER role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successfully retrieved payments"),
            @ApiResponse(responseCode = "403",
                    description = "Access denied if the user is not a manager"),
            @ApiResponse(responseCode = "404",
                    description = "User with input id not found")
    })
    public Page<PaymentDto> getAllPayments(
            @Parameter(description = "Optional user ID to filter payments;"
                    + " if omitted, returns all payments",
                    required = false)
            @RequestParam(required = false) Long userId,
            Pageable pageable) {
        if (userId == null) {
            return paymentService.findAll(pageable);
        }
        if (!userService.existsById(userId)) {
            throw new EntityNotFoundException("Cannot find payments. User with id="
                    + userId + " not found.");
        }
        return paymentService.findByUserId(userId, pageable);
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(
            summary = "Create a payment session",
            description = "Creates a new Stripe payment session for a specific rental and payment "
                    + "type (PAYMENT or FINE). "
                    + "Accessible by any user with the CUSTOMER role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Payment session created or retrieved successfully"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid request (no fine required, amount too small)"),
            @ApiResponse(responseCode = "403",
                    description = "Access denied"),
            @ApiResponse(responseCode = "404",
                    description = "Rental with the specified ID not found"),
            @ApiResponse(responseCode = "409",
                    description = "A payment of this type for this rental has already been paid"),
            @ApiResponse(responseCode = "503",
                    description = "Service Unavailable: Could not connect to the payment provider")
    })
    public PaymentResponseDto createPaymentSession(
            @Valid @RequestBody CreatePaymentRequestDto requestDto,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        return paymentService.createPaymentSession(requestDto, uriComponentsBuilder);
    }

    @GetMapping("/success")
    @Operation(
            summary = "Handle successful payment",
            description = "Public endpoint that Stripe redirects to after a successful payment. "
                    + "It verifies the payment status with Stripe and updates the internal record."
                    + "No authentication required."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Returns the status of the payment "
                    + "(e.g., SUCCESS, PENDING)"),
            @ApiResponse(responseCode = "404", description = "Payment session with the specified"
                    + " session_id not found in the database"),
            @ApiResponse(responseCode = "503",
                    description = "Service Unavailable: Could not connect to the payment provider")
    })
    public PaymentStatusResponseDto handleSuccessfulPayment(
            @RequestParam("session_id") String sessionId) {
        return paymentService.handleSuccessfulPayment(sessionId);
    }

    @GetMapping("/cancel")
    @Operation(
            summary = "Handle cancelled payment",
            description = "Public endpoint that Stripe redirects to if the user cancels "
                    + "the payment process. No authentication required."
    )
    @ApiResponse(responseCode = "200", description = "Returns a cancellation message and status")
    public PaymentStatusResponseDto handleCancelledPayment(
            @RequestParam("session_id") String sessionId) {
        return paymentService.handleCancelledPayment(sessionId);
    }

}

package com.loievroman.carsharingapp.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CustomGlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        List<String> errors = ex.getBindingResult()
                .getAllErrors().stream()
                .map(this::getErrorMessage)
                .toList();
        body.put("errors", errors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<String> handleEntityNotFoundExceptions(
            RegistrationException ex
    ) {
        return new ResponseEntity<>(
                ex.getMessage(),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundExceptions(
            EntityNotFoundException ex
    ) {
        return new ResponseEntity<>(
                ex.getMessage(),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(NoAvailableCarsException.class)
    public ResponseEntity<String> handleNoAvailableCarsExceptions(
            NoAvailableCarsException ex
    ) {
        return new ResponseEntity<>(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<String> handlePaymentExceptions(
            PaymentException ex
    ) {
        return new ResponseEntity<>(
                ex.getMessage(),
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    @ExceptionHandler(RentalAlreadyReturnedException.class)
    public ResponseEntity<String> handleRentalAlreadyReturnedExceptions(
            RentalAlreadyReturnedException ex
    ) {
        return new ResponseEntity<>(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(PaymentAlreadyPaidException.class)
    public ResponseEntity<String> handlePaymentAlreadyPaidExceptions(
            PaymentAlreadyPaidException ex
    ) {
        return new ResponseEntity<>(
                ex.getMessage(),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(NoFineRequiredException.class)
    public ResponseEntity<String> handleNoFineRequiredExceptions(
            NoFineRequiredException ex
    ) {
        return new ResponseEntity<>(
                ex.getMessage(),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(FineCalculationForActiveRentalException.class)
    public ResponseEntity<String> handleFineCalculationForActiveRentalExceptions(
            FineCalculationForActiveRentalException ex
    ) {
        return new ResponseEntity<>(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

    private String getErrorMessage(ObjectError objectError) {
        if (objectError instanceof FieldError fieldError) {
            return fieldError.getField()
                    + " "
                    + fieldError.getDefaultMessage();
        }
        return objectError.getDefaultMessage();
    }
}

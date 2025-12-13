package com.loievroman.carsharingapp.exception;

public class PaymentAlreadyPaidException extends RuntimeException {
    public PaymentAlreadyPaidException(String message) {
        super(message);
    }
}

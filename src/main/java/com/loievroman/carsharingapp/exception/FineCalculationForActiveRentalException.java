package com.loievroman.carsharingapp.exception;

public class FineCalculationForActiveRentalException extends RuntimeException {
    public FineCalculationForActiveRentalException(String message) {
        super(message);
    }
}

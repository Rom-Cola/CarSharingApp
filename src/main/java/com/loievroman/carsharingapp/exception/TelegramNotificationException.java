package com.loievroman.carsharingapp.exception;

public class TelegramNotificationException extends RuntimeException {
    public TelegramNotificationException(String message) {
        super(message);
    }

    public TelegramNotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}

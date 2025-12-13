package com.loievroman.carsharingapp.service;

import com.loievroman.carsharingapp.model.Payment;
import com.loievroman.carsharingapp.model.Rental;

public interface NotificationService {

    void sendNewRentalNotification(Rental rental);

    void sendNotification(String text);

    void sendPaymentConfirmedNotification(Payment payment);

    void sendOverdueRentalReminder(Rental rental);

    void sendRentalReturnedNotification(Rental rental);
}

package com.loievroman.carsharingapp.service;

import com.loievroman.carsharingapp.model.Payment;
import com.loievroman.carsharingapp.model.Rental;
import com.loievroman.carsharingapp.telegram.CarSharingBot;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramNotificationService implements NotificationService {

    private final CarSharingBot carSharingBot;

    @Value("${telegram.chat.id}")
    private String chatId;

    @Async
    @Override
    public void sendNewRentalNotification(Rental rental) {
        String message = String.format(
                "**New Rental!**\n"
                        + "Rental ID: %d\n"
                        + "Client: %s %s\n"
                        + "Car: %s %s\n"
                        + "Return date: %s",
                rental.getId(),
                rental.getUser().getFirstName(),
                rental.getUser().getLastName(),
                rental.getCar().getBrand(),
                rental.getCar().getModel(),
                rental.getReturnDate().toString()
        );
        carSharingBot.sendMessage(chatId, message);
    }

    @Async
    public void sendPaymentConfirmedNotification(Payment payment) {
        String message = String.format(
                "**Payment Confirmed!**\n"
                        + "Payment ID: %d\n"
                        + "Type: %s\n"
                        + "Amount: %s\n"
                        + "Rental ID: %d\n"
                        + "Client: %s %s\n"
                        + "Car: %s %s",
                payment.getId(),
                payment.getType().toString(),
                payment.getAmountToPay().toPlainString(),
                payment.getRental().getId(),
                payment.getRental().getUser().getFirstName(),
                payment.getRental().getUser().getLastName(),
                payment.getRental().getCar().getBrand(),
                payment.getRental().getCar().getModel()
        );
        carSharingBot.sendMessage(chatId, message);
    }

    @Async
    public void sendOverdueRentalReminder(Rental rental) {
        String message = String.format(
                "**Overdue Rental Reminder**\n"
                        + "Rental ID: %d\n"
                        + "Client: %s %s\n"
                        + "Car: %s %s\n"
                        + "Return date was: %s",
                rental.getId(),
                rental.getUser().getFirstName(),
                rental.getUser().getLastName(),
                rental.getCar().getBrand(),
                rental.getCar().getModel(),
                rental.getReturnDate().toString()
        );
        carSharingBot.sendMessage(chatId, message);
    }

    @Async
    @Override
    public void sendRentalReturnedNotification(Rental rental) {
        String message = String.format(
                "**Rental Returned!**\n"
                        + "Rental ID: %d\n"
                        + "Client: %s %s\n"
                        + "Car: %s %s\n"
                        + "Planned return date: %s\n"
                        + "Actual return date: %s",
                rental.getId(),
                rental.getUser().getFirstName(),
                rental.getUser().getLastName(),
                rental.getCar().getBrand(),
                rental.getCar().getModel(),
                rental.getReturnDate().toString(),
                rental.getActualReturnDate() != null ? rental.getActualReturnDate().toString() : "-"
        );
        carSharingBot.sendMessage(chatId, message);
    }

    @Override
    public void sendNotification(String text) {
        carSharingBot.sendMessage(chatId, text);
    }

}

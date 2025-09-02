package com.loievroman.carsharingapp.service;

import com.loievroman.carsharingapp.model.Rental;
import com.loievroman.carsharingapp.repository.RentalRepository;
import com.loievroman.carsharingapp.telegram.CarSharingBot;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramNotificationService implements NotificationService {

    private final RentalRepository rentalRepository;
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

    @Override
    public void sendNotification(String text) {
        carSharingBot.sendMessage(chatId, text);
    }

}

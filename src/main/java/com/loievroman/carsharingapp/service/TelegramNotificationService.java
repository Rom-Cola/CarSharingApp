package com.loievroman.carsharingapp.service;

import com.loievroman.carsharingapp.exception.EntityNotFoundException;
import com.loievroman.carsharingapp.model.Rental;
import com.loievroman.carsharingapp.repository.RentalRepository;
import com.loievroman.carsharingapp.telegram.CarSharingBot;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramNotificationService implements NotificationService {

    private final RentalRepository rentalRepository;
    private final CarSharingBot carSharingBot;

    private final String chatId = "-4913904012";

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

}

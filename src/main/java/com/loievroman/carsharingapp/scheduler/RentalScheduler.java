package com.loievroman.carsharingapp.scheduler;

import com.loievroman.carsharingapp.model.Rental;
import com.loievroman.carsharingapp.repository.RentalRepository;
import com.loievroman.carsharingapp.service.NotificationService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RentalScheduler {
    private final RentalRepository rentalRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 9 * * *")
    public void checkOverdueRentals() {
        List<Rental> overdueRentals = rentalRepository
                .findByActualReturnDateIsNullAndReturnDateBefore(LocalDate.now());

        if (overdueRentals.isEmpty()) {
            notificationService.sendNotification("No overdue rentals for today!");
        } else {
            StringBuilder stringBuilder = new StringBuilder("**OVERDUE RENTALS ALERT!**");
            for (Rental rental : overdueRentals) {
                stringBuilder.append(
                        String.format(
                                "\n\n  - Rental ID: %d\n"
                                        + "    Client: %s %s (ID: %d)\n"
                                        + "    Car: %s %s (ID: %d)\n"
                                        + "    Return date was: %s",
                                rental.getId(),
                                rental.getUser().getFirstName(),
                                rental.getUser().getLastName(),
                                rental.getUser().getId(),
                                rental.getCar().getBrand(),
                                rental.getCar().getModel(),
                                rental.getCar().getId(),
                                rental.getReturnDate().toString()
                     )
                );

            }
            notificationService.sendNotification(stringBuilder.toString());
        }
    }
}

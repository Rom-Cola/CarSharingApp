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
            for (Rental rental : overdueRentals) {
                notificationService.sendOverdueRentalReminder(rental);
            }
        }
    }
}

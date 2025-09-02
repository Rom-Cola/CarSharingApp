package com.loievroman.carsharingapp.service;

import com.loievroman.carsharingapp.model.Rental;

public interface NotificationService {

    void sendNewRentalNotification(Rental rental);

    void sendNotification(String text);
}

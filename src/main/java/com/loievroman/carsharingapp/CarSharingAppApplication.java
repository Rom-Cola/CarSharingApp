package com.loievroman.carsharingapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CarSharingAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarSharingAppApplication.class, args);
    }

}

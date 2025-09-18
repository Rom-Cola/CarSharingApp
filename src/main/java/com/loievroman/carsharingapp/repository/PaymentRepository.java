package com.loievroman.carsharingapp.repository;

import com.loievroman.carsharingapp.model.Payment;
import com.loievroman.carsharingapp.model.PaymentType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByRentalIdAndType(Long rentalId, PaymentType type);
}

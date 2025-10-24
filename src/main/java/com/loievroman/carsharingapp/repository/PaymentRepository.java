package com.loievroman.carsharingapp.repository;

import com.loievroman.carsharingapp.model.Payment;
import com.loievroman.carsharingapp.model.PaymentType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByRentalIdAndType(Long rentalId, PaymentType type);

    Optional<Payment> findBySessionId(String sessionId);

    @Query("SELECT p FROM Payment p JOIN p.rental r WHERE r.user.id = :userId")
    List<Payment> findByUserId(Long userId);
}

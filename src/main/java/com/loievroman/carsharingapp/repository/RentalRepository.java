package com.loievroman.carsharingapp.repository;

import com.loievroman.carsharingapp.model.Rental;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    Page<Rental> findByUserIdAndActualReturnDateIsNull(Long userId, Pageable pageable);

    Page<Rental> findByUserIdAndActualReturnDateIsNotNull(Long userId, Pageable pageable);

    Page<Rental> findByActualReturnDateIsNull(Pageable pageable);

    Page<Rental> findByActualReturnDateIsNotNull(Pageable pageable);

    List<Rental> findByActualReturnDateIsNullAndReturnDateBefore(LocalDate today);
}

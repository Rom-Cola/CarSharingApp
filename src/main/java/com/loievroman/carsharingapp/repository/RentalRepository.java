package com.loievroman.carsharingapp.repository;

import com.loievroman.carsharingapp.model.Rental;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findByUserIdAndActualReturnDateIsNull(Long userId);

    List<Rental> findByUserIdAndActualReturnDateIsNotNull(Long userId);

    List<Rental> findByActualReturnDateIsNull();

    List<Rental> findByActualReturnDateIsNotNull();
}

package com.loievroman.carsharingapp.repository;

import com.loievroman.carsharingapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}

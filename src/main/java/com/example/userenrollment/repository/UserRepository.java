package com.example.userenrollment.repository;

import com.example.userenrollment.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmailIdIgnoreCase(String emailId);

    Optional<User> findByEmailIdIgnoreCase(String emailId);
}

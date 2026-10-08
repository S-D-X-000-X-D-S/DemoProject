package com.example.userenrollment.repository;

import com.example.userenrollment.model.EmailVerificationChallenge;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface EmailVerificationChallengeRepository extends JpaRepository<EmailVerificationChallenge, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select challenge from EmailVerificationChallenge challenge where challenge.emailId = :emailId")
    Optional<EmailVerificationChallenge> findByEmailIdForUpdate(@Param("emailId") String emailId);

    Optional<EmailVerificationChallenge> findByRegistrationTokenHashAndRegistrationTokenExpiresAtAfter(
            String registrationTokenHash,
            Instant now
    );
}

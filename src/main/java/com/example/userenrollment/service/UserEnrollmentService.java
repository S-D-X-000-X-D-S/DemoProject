package com.example.userenrollment.service;

import com.example.userenrollment.dto.EnrollmentRequest;
import com.example.userenrollment.model.EmailVerificationChallenge;
import com.example.userenrollment.model.User;
import com.example.userenrollment.repository.EmailVerificationChallengeRepository;
import com.example.userenrollment.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class UserEnrollmentService {
    private final UserRepository userRepository;
    private final EmailVerificationChallengeRepository challengeRepository;
    private final PasswordEncoder passwordEncoder;

    public UserEnrollmentService(
            UserRepository userRepository,
            EmailVerificationChallengeRepository challengeRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.challengeRepository = challengeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User enroll(EnrollmentRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new PasswordConfirmationException();
        }

        String tokenHash = EmailVerificationService.hashToken(request.registrationToken());
        EmailVerificationChallenge challenge = challengeRepository
                .findByRegistrationTokenHashAndRegistrationTokenExpiresAtAfter(tokenHash, Instant.now())
                .orElseThrow(() -> new EnrollmentFlowException(
                        HttpStatus.GONE,
                        "registrationToken",
                        "Email verification has expired. Verify your email again."
                ));
        String emailId = challenge.getEmailId();
        if (userRepository.existsByEmailIdIgnoreCase(emailId)) {
            throw new UserAlreadyExistsException();
        }

        User user = new User(
                request.name().trim(),
                emailId,
                passwordEncoder.encode(request.password())
        );

        try {
            User savedUser = userRepository.saveAndFlush(user);
            challengeRepository.delete(challenge);
            return savedUser;
        } catch (DataIntegrityViolationException ex) {
            if (isEmailConstraintViolation(ex)) {
                throw new UserAlreadyExistsException();
            }
            throw ex;
        }
    }

    private boolean isEmailConstraintViolation(DataIntegrityViolationException exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof org.hibernate.exception.ConstraintViolationException violation) {
                return "uk_users_email_id".equals(violation.getConstraintName());
            }
            cause = cause.getCause();
        }
        return false;
    }
}

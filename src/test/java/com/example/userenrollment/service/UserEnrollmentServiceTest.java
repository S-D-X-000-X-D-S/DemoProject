package com.example.userenrollment.service;

import com.example.userenrollment.dto.EnrollmentRequest;
import com.example.userenrollment.model.EmailVerificationChallenge;
import com.example.userenrollment.model.User;
import com.example.userenrollment.repository.EmailVerificationChallengeRepository;
import com.example.userenrollment.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserEnrollmentServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationChallengeRepository challengeRepository;

    private PasswordEncoder passwordEncoder;
    private UserEnrollmentService service;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        service = new UserEnrollmentService(userRepository, challengeRepository, passwordEncoder);
    }

    @Test
    void verifiedEmailIsBoundToRegistrationTokenAndPasswordIsHashed() {
        String token = "verified-registration-token";
        EmailVerificationChallenge challenge = new EmailVerificationChallenge(
                "alex@example.com",
                passwordEncoder.encode("123456"),
                Instant.now().plusSeconds(300),
                Instant.now()
        );
        challenge.verifyPasscode(EmailVerificationService.hashToken(token), Instant.now().plusSeconds(600));
        when(challengeRepository.findByRegistrationTokenHashAndRegistrationTokenExpiresAtAfter(
                eq(EmailVerificationService.hashToken(token)),
                org.mockito.ArgumentMatchers.any(Instant.class)
        )).thenReturn(Optional.of(challenge));
        when(userRepository.existsByEmailIdIgnoreCase("alex@example.com")).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = service.enroll(new EnrollmentRequest("Alex Doe", "correct-horse-7", "correct-horse-7", token));

        assertEquals("alex@example.com", saved.getEmailId());
        assertTrue(passwordEncoder.matches("correct-horse-7", saved.getPasswordHash()));
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());
        assertEquals("alex@example.com", userCaptor.getValue().getEmailId());
        verify(challengeRepository).delete(challenge);
    }
}

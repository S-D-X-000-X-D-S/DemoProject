package com.example.userenrollment.service;

import com.example.userenrollment.model.EmailVerificationChallenge;
import com.example.userenrollment.repository.EmailVerificationChallengeRepository;
import com.example.userenrollment.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationChallengeRepository challengeRepository;

    @Mock
    private EmailSender emailSender;

    private PasswordEncoder passwordEncoder;
    private EmailVerificationService service;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        service = new EmailVerificationService(
                userRepository,
                challengeRepository,
                passwordEncoder,
                emailSender
        );
    }

    @Test
    void existingEmailSelectsLoginWithoutSendingCode() {
        when(userRepository.existsByEmailIdIgnoreCase("member@example.com")).thenReturn(true);

        EmailVerificationService.EmailFlowResult result = service.start("Member@Example.com");

        assertTrue(result.existingUser());
        verify(emailSender, never()).sendVerificationPasscode(any(), any());
        verify(challengeRepository, never()).save(any());
    }

    @Test
    void newEmailReceivesSixDigitCodeAndOnlyHashIsStored() {
        when(userRepository.existsByEmailIdIgnoreCase("new@example.com")).thenReturn(false);
        when(challengeRepository.findById("new@example.com")).thenReturn(Optional.empty());
        ArgumentCaptor<EmailVerificationChallenge> challengeCaptor =
                ArgumentCaptor.forClass(EmailVerificationChallenge.class);
        ArgumentCaptor<String> passcodeCaptor = ArgumentCaptor.forClass(String.class);

        EmailVerificationService.EmailFlowResult result = service.start("New@Example.com");

        assertFalse(result.existingUser());
        verify(challengeRepository).save(challengeCaptor.capture());
        verify(emailSender).sendVerificationPasscode(org.mockito.ArgumentMatchers.eq("new@example.com"), passcodeCaptor.capture());
        String passcode = passcodeCaptor.getValue();
        assertTrue(passcode.matches("\\d{6}"));
        assertTrue(passwordEncoder.matches(passcode, challengeCaptor.getValue().getPasscodeHash()));
        assertTrue(challengeCaptor.getValue().getPasscodeExpiresAt().isAfter(Instant.now()));
    }

    @Test
    void wrongPasscodeReturnsSpecificValidationErrorAndCountsAttempt() {
        EmailVerificationChallenge challenge = challenge("123456");
        when(challengeRepository.findByEmailIdForUpdate("new@example.com")).thenReturn(Optional.of(challenge));

        EnrollmentFlowException exception = assertThrows(
                EnrollmentFlowException.class,
                () -> service.verifyPasscode("new@example.com", "000000")
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("passcode", exception.getField());
        assertEquals(1, challenge.getAttempts());
    }

    @Test
    void correctPasscodeCreatesShortLivedRegistrationToken() {
        EmailVerificationChallenge challenge = challenge("123456");
        when(challengeRepository.findByEmailIdForUpdate("new@example.com")).thenReturn(Optional.of(challenge));

        String token = service.verifyPasscode("new@example.com", "123456");

        assertNotEquals("123456", token);
        assertEquals(64, challenge.getRegistrationTokenHash().length());
        assertEquals(
                EmailVerificationService.hashToken(token),
                challenge.getRegistrationTokenHash()
        );
        assertTrue(challenge.getRegistrationTokenExpiresAt().isAfter(Instant.now()));
    }

    @Test
    void expiredPasscodeCannotBeUsed() {
        Instant now = Instant.now();
        EmailVerificationChallenge challenge = new EmailVerificationChallenge(
                "new@example.com",
                passwordEncoder.encode("123456"),
                now.minusSeconds(1),
                now.minusSeconds(301)
        );
        when(challengeRepository.findByEmailIdForUpdate("new@example.com")).thenReturn(Optional.of(challenge));

        EnrollmentFlowException exception = assertThrows(
                EnrollmentFlowException.class,
                () -> service.verifyPasscode("new@example.com", "123456")
        );

        assertEquals(HttpStatus.GONE, exception.getStatus());
        assertEquals("passcode", exception.getField());
    }

    private EmailVerificationChallenge challenge(String passcode) {
        Instant now = Instant.now();
        return new EmailVerificationChallenge(
                "new@example.com",
                passwordEncoder.encode(passcode),
                now.plusSeconds(300),
                now.minusSeconds(60)
        );
    }
}

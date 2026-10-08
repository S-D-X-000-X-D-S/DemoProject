package com.example.userenrollment.service;

import com.example.userenrollment.model.EmailVerificationChallenge;
import com.example.userenrollment.repository.EmailVerificationChallengeRepository;
import com.example.userenrollment.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class EmailVerificationService {
    private static final Duration PASSCODE_LIFETIME = Duration.ofMinutes(5);
    private static final Duration RESEND_INTERVAL = Duration.ofSeconds(60);
    private static final Duration REGISTRATION_TOKEN_LIFETIME = Duration.ofMinutes(10);
    private static final int MAX_PASSCODE_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final EmailVerificationChallengeRepository challengeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSender emailSender;
    private final SecureRandom secureRandom = new SecureRandom();

    public EmailVerificationService(
            UserRepository userRepository,
            EmailVerificationChallengeRepository challengeRepository,
            PasswordEncoder passwordEncoder,
            EmailSender emailSender
    ) {
        this.userRepository = userRepository;
        this.challengeRepository = challengeRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailSender = emailSender;
    }

    @Transactional
    public EmailFlowResult start(String submittedEmail) {
        String email = normalize(submittedEmail);
        if (userRepository.existsByEmailIdIgnoreCase(email)) {
            return EmailFlowResult.login();
        }

        Instant now = Instant.now();
        EmailVerificationChallenge challenge = challengeRepository.findById(email).orElse(null);
        if (challenge != null && challenge.getLastSentAt().plus(RESEND_INTERVAL).isAfter(now)) {
            throw new EnrollmentFlowException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "emailId",
                    "A verification code was sent recently. Wait 60 seconds before requesting another."
            );
        }

        String passcode = String.format(Locale.ROOT, "%06d", secureRandom.nextInt(1_000_000));
        String passcodeHash = passwordEncoder.encode(passcode);
        if (challenge == null) {
            challenge = new EmailVerificationChallenge(
                    email,
                    passcodeHash,
                    now.plus(PASSCODE_LIFETIME),
                    now
            );
        } else {
            challenge.issuePasscode(passcodeHash, now.plus(PASSCODE_LIFETIME), now);
        }
        challengeRepository.save(challenge);
        emailSender.sendVerificationPasscode(email, passcode);
        return EmailFlowResult.verifyEmail();
    }

    @Transactional(noRollbackFor = EnrollmentFlowException.class)
    public String verifyPasscode(String submittedEmail, String passcode) {
        String email = normalize(submittedEmail);
        EmailVerificationChallenge challenge = challengeRepository.findByEmailIdForUpdate(email)
                .orElseThrow(() -> flow(HttpStatus.BAD_REQUEST, "passcode", "Verification code is invalid or expired."));

        Instant now = Instant.now();
        if (!challenge.getPasscodeExpiresAt().isAfter(now)) {
            throw flow(HttpStatus.GONE, "passcode", "Verification code has expired. Request a new code.");
        }
        if (challenge.getAttempts() >= MAX_PASSCODE_ATTEMPTS) {
            throw flow(HttpStatus.TOO_MANY_REQUESTS, "passcode", "Too many incorrect attempts. Request a new code.");
        }
        if (!passwordEncoder.matches(passcode, challenge.getPasscodeHash())) {
            challenge.recordFailedAttempt();
            if (challenge.getAttempts() >= MAX_PASSCODE_ATTEMPTS) {
                throw flow(HttpStatus.TOO_MANY_REQUESTS, "passcode", "Too many incorrect attempts. Request a new code.");
            }
            throw flow(HttpStatus.BAD_REQUEST, "passcode", "Verification code is incorrect.");
        }

        String token = newRegistrationToken();
        challenge.verifyPasscode(hashToken(token), now.plus(REGISTRATION_TOKEN_LIFETIME));
        return token;
    }

    public static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public static String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable.", exception);
        }
    }

    private String newRegistrationToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private EnrollmentFlowException flow(HttpStatus status, String field, String message) {
        return new EnrollmentFlowException(status, field, message);
    }

    public record EmailFlowResult(boolean existingUser) {
        public static EmailFlowResult login() {
            return new EmailFlowResult(true);
        }

        public static EmailFlowResult verifyEmail() {
            return new EmailFlowResult(false);
        }
    }
}

package com.example.userenrollment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "email_verification_challenges")
public class EmailVerificationChallenge {
    @Id
    @Column(name = "email_id", nullable = false, length = 254)
    private String emailId;

    @Column(name = "passcode_hash", nullable = false, length = 100)
    private String passcodeHash;

    @Column(name = "passcode_expires_at", nullable = false)
    private Instant passcodeExpiresAt;

    @Column(name = "last_sent_at", nullable = false)
    private Instant lastSentAt;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "registration_token_hash", length = 64)
    private String registrationTokenHash;

    @Column(name = "registration_token_expires_at")
    private Instant registrationTokenExpiresAt;

    protected EmailVerificationChallenge() {
    }

    public EmailVerificationChallenge(String emailId, String passcodeHash, Instant passcodeExpiresAt, Instant lastSentAt) {
        this.emailId = emailId;
        this.passcodeHash = passcodeHash;
        this.passcodeExpiresAt = passcodeExpiresAt;
        this.lastSentAt = lastSentAt;
    }

    public String getEmailId() {
        return emailId;
    }

    public String getPasscodeHash() {
        return passcodeHash;
    }

    public Instant getPasscodeExpiresAt() {
        return passcodeExpiresAt;
    }

    public Instant getLastSentAt() {
        return lastSentAt;
    }

    public int getAttempts() {
        return attempts;
    }

    public String getRegistrationTokenHash() {
        return registrationTokenHash;
    }

    public Instant getRegistrationTokenExpiresAt() {
        return registrationTokenExpiresAt;
    }

    public void issuePasscode(String passcodeHash, Instant expiresAt, Instant sentAt) {
        this.passcodeHash = passcodeHash;
        this.passcodeExpiresAt = expiresAt;
        this.lastSentAt = sentAt;
        this.attempts = 0;
        this.registrationTokenHash = null;
        this.registrationTokenExpiresAt = null;
    }

    public void recordFailedAttempt() {
        attempts++;
    }

    public void verifyPasscode(String tokenHash, Instant tokenExpiresAt) {
        this.registrationTokenHash = tokenHash;
        this.registrationTokenExpiresAt = tokenExpiresAt;
    }
}

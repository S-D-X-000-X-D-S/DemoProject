package com.example.userenrollment.service;

public interface EmailSender {
    void sendVerificationPasscode(String recipient, String passcode);
}

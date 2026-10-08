package com.example.userenrollment.service;

public class PasswordConfirmationException extends RuntimeException {
    public PasswordConfirmationException() {
        super("Password and confirmation do not match.");
    }
}

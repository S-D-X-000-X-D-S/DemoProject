package com.example.userenrollment.service;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException() {
        super("This email address is already registered. Continue to login.");
    }
}

package com.example.userenrollment.dto;

public record VerificationResponse(
        int statusCode,
        String nextStep,
        String registrationToken,
        int expiresInSeconds
) {
}

package com.example.userenrollment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnrollmentRequest(
        @NotBlank(message = "Name is required.")
        @Size(max = 100, message = "Name must be 100 characters or fewer.")
        String name,

        @NotBlank(message = "Password is required.")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters.")
        String password,

        @NotBlank(message = "Password confirmation is required.")
        String confirmPassword,

        @NotBlank(message = "Registration token is required.")
        @Size(max = 100, message = "Registration token is invalid.")
        String registrationToken
) {
}

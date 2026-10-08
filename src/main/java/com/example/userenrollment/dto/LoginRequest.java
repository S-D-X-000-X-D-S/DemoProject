package com.example.userenrollment.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Email address is required.")
        @Email(message = "Email address is invalid.")
        @Size(max = 254, message = "Email address must be 254 characters or fewer.")
        String emailId,

        @NotBlank(message = "Password is required.")
        @Size(max = 72, message = "Password must be 72 characters or fewer.")
        String password
) {
}

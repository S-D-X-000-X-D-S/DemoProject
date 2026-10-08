package com.example.userenrollment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record PasscodeVerificationRequest(
        @NotBlank(message = "Email address is required.")
        @Email(message = "Email address is invalid.")
        @Size(max = 254, message = "Email address must be 254 characters or fewer.")
        String emailId,

        @NotBlank(message = "Passcode is required.")
        @Pattern(regexp = "^\\d{6}$", message = "Passcode must contain exactly 6 digits.")
        String passcode
) {
}

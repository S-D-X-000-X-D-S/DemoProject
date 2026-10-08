package com.example.userenrollment.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmailRequest(
        @NotBlank(message = "Email address is required.")
        @Email(message = "Email address is invalid.")
        @Size(max = 254, message = "Email address must be 254 characters or fewer.")
        String emailId
) {
}

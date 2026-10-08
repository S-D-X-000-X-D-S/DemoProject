package com.example.userenrollment.controller;

import com.example.userenrollment.dto.EmailRequest;
import com.example.userenrollment.dto.EnrollmentResponse;
import com.example.userenrollment.dto.FlowResponse;
import com.example.userenrollment.dto.LoginRequest;
import com.example.userenrollment.dto.PasscodeVerificationRequest;
import com.example.userenrollment.dto.VerificationResponse;
import com.example.userenrollment.service.EmailVerificationService;
import com.example.userenrollment.service.UserLoginService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final EmailVerificationService emailVerificationService;
    private final UserLoginService userLoginService;

    public AuthController(EmailVerificationService emailVerificationService, UserLoginService userLoginService) {
        this.emailVerificationService = emailVerificationService;
        this.userLoginService = userLoginService;
    }

    @PostMapping("/email")
    public FlowResponse checkEmail(@Valid @RequestBody EmailRequest request) {
        boolean existingUser = emailVerificationService.start(request.emailId()).existingUser();
        return existingUser
                ? new FlowResponse(HttpStatus.OK.value(), "LOGIN", "Enter your password to log in.")
                : new FlowResponse(HttpStatus.OK.value(), "VERIFY_EMAIL", "A verification code was sent to your email.");
    }

    @PostMapping("/verify-email")
    public VerificationResponse verifyEmail(@Valid @RequestBody PasscodeVerificationRequest request) {
        String registrationToken = emailVerificationService.verifyPasscode(request.emailId(), request.passcode());
        return new VerificationResponse(
                HttpStatus.OK.value(),
                "COMPLETE_REGISTRATION",
                registrationToken,
                600
        );
    }

    @PostMapping("/login")
    public EnrollmentResponse login(@Valid @RequestBody LoginRequest request) {
        var user = userLoginService.login(request);
        return new EnrollmentResponse(HttpStatus.OK.value(), "Hi " + user.getName() + ", welcome back to MovieKnight.");
    }
}

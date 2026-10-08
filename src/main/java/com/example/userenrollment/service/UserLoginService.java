package com.example.userenrollment.service;

import com.example.userenrollment.dto.LoginRequest;
import com.example.userenrollment.model.User;
import com.example.userenrollment.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserLoginService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserLoginService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User login(LoginRequest request) {
        User user = userRepository.findByEmailIdIgnoreCase(
                        EmailVerificationService.normalize(request.emailId())
                )
                .orElseThrow(this::invalidCredentials);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }
        return user;
    }

    private EnrollmentFlowException invalidCredentials() {
        return new EnrollmentFlowException(
                HttpStatus.UNAUTHORIZED,
                "credentials",
                "Email address or password is incorrect."
        );
    }
}

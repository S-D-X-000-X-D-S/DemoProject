package com.example.userenrollment.service;

import com.example.userenrollment.dto.LoginRequest;
import com.example.userenrollment.model.User;
import com.example.userenrollment.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserLoginServiceTest {
    @Mock
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;
    private UserLoginService service;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        service = new UserLoginService(userRepository, passwordEncoder);
    }

    @Test
    void loginNormalizesEmailAndVerifiesPassword() {
        User user = new User("Alex Doe", "alex@example.com", passwordEncoder.encode("correct-horse-7"));
        when(userRepository.findByEmailIdIgnoreCase("alex@example.com")).thenReturn(Optional.of(user));

        User loggedIn = service.login(new LoginRequest("Alex@Example.com", "correct-horse-7"));

        assertEquals("Alex Doe", loggedIn.getName());
    }

    @Test
    void incorrectPasswordReturnsGenericCredentialError() {
        User user = new User("Alex Doe", "alex@example.com", passwordEncoder.encode("correct-horse-7"));
        when(userRepository.findByEmailIdIgnoreCase("alex@example.com")).thenReturn(Optional.of(user));

        EnrollmentFlowException exception = assertThrows(
                EnrollmentFlowException.class,
                () -> service.login(new LoginRequest("alex@example.com", "wrong-password"))
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("Email address or password is incorrect.", exception.getMessage());
    }
}

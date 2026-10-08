package com.example.userenrollment.controller;

import com.example.userenrollment.dto.ValidationErrorResponse;
import com.example.userenrollment.service.EnrollmentFlowException;
import com.example.userenrollment.service.PasswordConfirmationException;
import com.example.userenrollment.service.UserAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class EnrollmentExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnrollmentExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = exception.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage() == null ? "Invalid value." : error.getDefaultMessage(),
                        (first, duplicate) -> first,
                        LinkedHashMap::new
                ));
        return new ValidationErrorResponse(HttpStatus.BAD_REQUEST.value(), errors);
    }

    @ExceptionHandler(PasswordConfirmationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handlePasswordConfirmation(PasswordConfirmationException exception) {
        return new ValidationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                Map.of("confirmPassword", exception.getMessage())
        );
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ValidationErrorResponse handleDuplicateUser(UserAlreadyExistsException exception) {
        return new ValidationErrorResponse(
                HttpStatus.CONFLICT.value(),
                Map.of("emailId", exception.getMessage())
        );
    }

    @ExceptionHandler(EnrollmentFlowException.class)
    public ResponseEntity<ValidationErrorResponse> handleEnrollmentFlow(EnrollmentFlowException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(new ValidationErrorResponse(
                        exception.getStatus().value(),
                        Map.of(exception.getField(), exception.getMessage())
                ));
    }

    @ExceptionHandler(MailException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ValidationErrorResponse handleMailFailure(MailException exception) {
        LOGGER.error("Verification email delivery failed.", exception);
        return new ValidationErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                Map.of("emailId", "Verification email could not be sent. Check the mail server configuration and try again.")
        );
    }
}

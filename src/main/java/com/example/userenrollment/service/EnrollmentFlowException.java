package com.example.userenrollment.service;

import org.springframework.http.HttpStatus;

public class EnrollmentFlowException extends RuntimeException {
    private final HttpStatus status;
    private final String field;

    public EnrollmentFlowException(HttpStatus status, String field, String message) {
        super(message);
        this.status = status;
        this.field = field;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getField() {
        return field;
    }
}

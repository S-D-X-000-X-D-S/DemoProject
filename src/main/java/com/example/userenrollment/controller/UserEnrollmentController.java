package com.example.userenrollment.controller;

import com.example.userenrollment.dto.EnrollmentRequest;
import com.example.userenrollment.dto.EnrollmentResponse;
import com.example.userenrollment.service.UserEnrollmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserEnrollmentController {
    private final UserEnrollmentService enrollmentService;

    public UserEnrollmentController(UserEnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentResponse enroll(@Valid @RequestBody EnrollmentRequest request) {
        var user = enrollmentService.enroll(request);
        return new EnrollmentResponse(HttpStatus.CREATED.value(), "Hi " + user.getName() + ", welcome to MovieKnight."
        );
    }
}

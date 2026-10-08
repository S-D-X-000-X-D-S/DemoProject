package com.example.userenrollment.dto;

import java.util.Map;

public record ValidationErrorResponse(int statusCode, Map<String, String> errors) {
}

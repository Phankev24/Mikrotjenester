package org.example.vendor.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

/** Turns exceptions into consistent JSON (RFC 7807 "problem detail"). */
@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    /** Our own 404/409/502... with the message we wrote. */
    @ExceptionHandler(ResponseStatusException.class)
    ProblemDetail handleStatus(ResponseStatusException e) {
        if (e.getStatusCode().is5xxServerError()) {
            log.error("Request failed with {}: {}", e.getStatusCode().value(), e.getReason());
        } else {
            log.warn("Request rejected with {}: {}", e.getStatusCode().value(), e.getReason());
        }
        return ProblemDetail.forStatusAndDetail(e.getStatusCode(), e.getReason());
    }

    /** Bean validation failures: 400 with one message per field. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError f : e.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(f.getField(), String.valueOf(f.getDefaultMessage()));
        }
        log.warn("Validation failed for fields {}", errors.keySet());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problem.setProperty("errors", errors);
        return problem;
    }

    /** A unique constraint (email) lost a race with the pre-check. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail handleIntegrity(DataIntegrityViolationException e) {
        log.warn("Data integrity violation rejected with 409: {}", e.getClass().getSimpleName());
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Value already exists");
    }
}

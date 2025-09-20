package ch.notenverwaltung.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                            HttpServletRequest request) {
        return buildValidationErrorResponse(ex.getBindingResult().getFieldErrors(), request);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, Object>> handleBindException(BindException ex,
                                                                   HttpServletRequest request) {
        return buildValidationErrorResponse(ex.getBindingResult().getFieldErrors(), request);
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleAlreadyExists(AlreadyExistsException ex,
                                                                   HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", ex.getMessage());
        body.put("path", request != null ? request.getRequestURI() : null);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex,
                                                                            HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", "Operation violates referential integrity or unique constraints");
        body.put("path", request != null ? request.getRequestURI() : null);
        return ResponseEntity.status(status).body(body);
    }

    private ResponseEntity<Map<String, Object>> buildValidationErrorResponse(List<FieldError> fieldErrors,
                                                                             HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        // Collect field errors; if multiple per field, keep all messages
        Map<String, List<String>> validationErrors = new LinkedHashMap<>();
        for (FieldError fe : fieldErrors) {
            validationErrors.computeIfAbsent(fe.getField(), k -> new LinkedList<>())
                    .add(fe.getDefaultMessage());
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", "Validation failed");
        body.put("path", request != null ? request.getRequestURI() : null);
        body.put("validationErrors", validationErrors);

        return ResponseEntity.status(status).body(body);
    }
}

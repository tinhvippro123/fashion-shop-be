package com.fashionshop.be.exception;

import com.fashionshop.be.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        HttpStatus status = resolveHttpStatus(errorCode);
        return ResponseEntity.status(status)
                .body(ApiResponse.error(errorCode != null ? errorCode.name() : "ERROR", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        return ResponseEntity.status(resolveHttpStatus(errorCode))
                .body(ApiResponse.error(errorCode.name(), msg.isBlank() ? errorCode.getMessage() : msg));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        ErrorCode errorCode = ErrorCode.INVALID_CREDENTIALS;
        return ResponseEntity.status(resolveHttpStatus(errorCode))
                .body(ApiResponse.error(errorCode.name(), errorCode.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        return ResponseEntity.status(resolveHttpStatus(errorCode))
                .body(ApiResponse.error(errorCode.name(), errorCode.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;
        return ResponseEntity.status(resolveHttpStatus(errorCode))
                .body(ApiResponse.error(errorCode.name(), errorCode.getMessage()));
    }

    private HttpStatus resolveHttpStatus(ErrorCode errorCode) {
        if (errorCode == null) {
            return HttpStatus.BAD_REQUEST;
        }

        String name = errorCode.name();
        if (name.contains("NOT_FOUND") || name.equals("INVALID_PRODUCT_SLUG")) {
            return HttpStatus.NOT_FOUND;
        }
        if (name.equals("UNAUTHENTICATED") || name.equals("INVALID_CREDENTIALS")) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (name.equals("UNAUTHORIZED") || name.contains("ACCESS_DENIED") || name.contains("LOCKED")) {
            return HttpStatus.FORBIDDEN;
        }
        if (name.equals("UNCATEGORIZED_EXCEPTION")) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return HttpStatus.BAD_REQUEST;
    }
}

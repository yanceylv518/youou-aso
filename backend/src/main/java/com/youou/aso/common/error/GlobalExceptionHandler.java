package com.youou.aso.common.error;

import com.youou.aso.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex, Locale locale, HttpServletRequest request) {
        String message = messageSource.getMessage("error." + ex.getErrorCode().name(), null, ex.getErrorCode().name(), locale);
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getErrorCode().name(), message, ex.getArgs(), request.getHeader("X-Request-Id")));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResponse<Void>> handleValidation(Exception ex, Locale locale, HttpServletRequest request) {
        FieldError fieldError = null;
        if (ex instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            fieldError = methodArgumentNotValidException.getBindingResult().getFieldError();
        } else if (ex instanceof BindException bindException) {
            fieldError = bindException.getBindingResult().getFieldError();
        }
        String message = fieldError == null
                ? messageSource.getMessage("error.VALIDATION_ERROR", null, "Validation error", locale)
                : fieldError.getField() + ": " + fieldError.getDefaultMessage();
        return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.VALIDATION_ERROR.name(), message, null, request.getHeader("X-Request-Id")));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex, Locale locale, HttpServletRequest request) {
        String message = messageSource.getMessage("error.SYSTEM_ERROR", null, "System error", locale);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.SYSTEM_ERROR.name(), message, null, request.getHeader("X-Request-Id")));
    }
}

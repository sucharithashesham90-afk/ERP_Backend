package com.erp.platform.common.exception;

import com.erp.platform.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        log.warn("AppException: code={} message={}", ex.getErrorCode().getCode(), ex.getMessage());
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(ApiResponse.error(ex.getErrorCode().getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest()
                .body(ApiResponse.validationError(errors));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(403)
                .body(ApiResponse.error(ErrorCode.FORBIDDEN.getCode(), "Access denied"));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuth(AuthenticationException ex) {
        return ResponseEntity.status(401)
                .body(ApiResponse.error(ErrorCode.UNAUTHORIZED.getCode(), "Authentication required"));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NoResourceFoundException ex) {
        return ResponseEntity.status(404)
                .body(ApiResponse.error("ERR_404", "Endpoint not found: " + ex.getResourcePath()));
    }

    // DB constraint failures (not-null, too-long, duplicate) — surface the real reason
    // instead of an opaque ERR_010 so the user can act on it.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        Throwable root = NestedExceptionUtils.getMostSpecificCause(ex);
        String raw = root.getMessage() == null ? "" : root.getMessage();
        String first = raw.split("\\r?\\n")[0].trim();
        log.warn("DataIntegrityViolation: {}", raw);
        boolean duplicate = first.toLowerCase().contains("duplicate key")
                || first.toLowerCase().contains("unique constraint");
        ErrorCode code = duplicate ? ErrorCode.DUPLICATE_VALUE : ErrorCode.VALIDATION_FAILED;
        return ResponseEntity.status(duplicate ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(code.getCode(), friendlyDbMessage(first)));
    }

    private static String friendlyDbMessage(String dbMsg) {
        String m = dbMsg == null ? "" : dbMsg;
        String lower = m.toLowerCase();
        if (lower.contains("value too long"))
            return "A field value is too long. Please shorten it and try again.";
        if (lower.contains("violates not-null") || lower.contains("null value in column")) {
            String col = between(m, "column \"", "\"");
            return col != null ? "Required field is missing: " + col : "A required field is missing.";
        }
        if (lower.contains("duplicate key") || lower.contains("unique constraint")) {
            // Name what actually collided. An unqualified "already exists" sent people hunting for a
            // duplicate of the record they were creating, when the clash is often on a row they
            // never see — an auto-created ledger group, a soft-deleted row still holding its code.
            String detail = between(m, "Key (", ")");
            if (detail != null && !detail.isBlank()) {
                return "A record with these details already exists — " + detail.trim() + " is taken.";
            }
            String constraint = between(m, "constraint \"", "\"");
            if (constraint != null && !constraint.isBlank()) {
                return "A record with these details already exists (" + constraint.trim() + ").";
            }
            return "A record with these details already exists.";
        }
        return m.isBlank() ? "Could not save due to a data constraint." : "Could not save: " + m;
    }

    private static String between(String s, String a, String b) {
        int i = s.indexOf(a);
        if (i < 0) return null;
        int j = s.indexOf(b, i + a.length());
        return j < 0 ? null : s.substring(i + a.length(), j);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), "Internal server error"));
    }
}

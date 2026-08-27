package com.erp.platform.common;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.exception.ErrorCode;
import com.erp.platform.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("GlobalExceptionHandler unit tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // ─── AppException ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("handleAppException() returns 404 for NOT_FOUND")
    void handleApp_notFound() {
        AppException ex = AppException.notFound("Resource X not found");
        ResponseEntity<ApiResponse<Void>> response = handler.handleAppException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND.getCode());
    }

    @Test
    @DisplayName("handleAppException() returns 409 for CONFLICT")
    void handleApp_conflict() {
        AppException ex = AppException.conflict("Duplicate code");
        ResponseEntity<ApiResponse<Void>> response = handler.handleAppException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.CONFLICT.getCode());
    }

    @Test
    @DisplayName("handleAppException() returns 403 for FORBIDDEN")
    void handleApp_forbidden() {
        AppException ex = AppException.forbidden("Cannot delete system account");
        ResponseEntity<ApiResponse<Void>> response = handler.handleAppException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN.getCode());
    }

    @Test
    @DisplayName("handleAppException() returns 422 for BUSINESS_RULE_VIOLATION")
    void handleApp_businessRule() {
        AppException ex = AppException.badRequest("Only DRAFT can be posted");
        ResponseEntity<ApiResponse<Void>> response = handler.handleAppException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // ─── MethodArgumentNotValidException ──────────────────────────────────────

    @Test
    @DisplayName("handleValidation() returns 400 with field error messages")
    void handleValidation_badRequest() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("obj", "email", "must not be blank");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ApiResponse<Void>> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("handleValidation() includes multiple field errors")
    void handleValidation_multipleErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("obj", "name", "must not be blank"),
                new FieldError("obj", "email", "invalid email")
        ));

        ResponseEntity<ApiResponse<Void>> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ─── AccessDeniedException ────────────────────────────────────────────────

    @Test
    @DisplayName("handleAccessDenied() returns 403")
    void handleAccessDenied() {
        AccessDeniedException ex = new AccessDeniedException("forbidden");
        ResponseEntity<ApiResponse<Void>> response = handler.handleAccessDenied(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(403);
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN.getCode());
    }

    // ─── AuthenticationException ──────────────────────────────────────────────

    @Test
    @DisplayName("handleAuth() returns 401")
    void handleAuth() {
        AuthenticationException ex = mock(AuthenticationException.class);
        ResponseEntity<ApiResponse<Void>> response = handler.handleAuth(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED.getCode());
    }

    // ─── NoResourceFoundException ─────────────────────────────────────────────

    @Test
    @DisplayName("handleNotFound() returns 404 for unknown endpoint")
    void handleNotFound() throws Exception {
        NoResourceFoundException ex = mock(NoResourceFoundException.class);
        when(ex.getResourcePath()).thenReturn("/api/v1/unknown");
        ResponseEntity<ApiResponse<Void>> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).contains("/api/v1/unknown");
    }

    // ─── General Exception ────────────────────────────────────────────────────

    @Test
    @DisplayName("handleGeneral() returns 500 for unexpected exceptions")
    void handleGeneral() {
        RuntimeException ex = new RuntimeException("Unexpected failure");
        ResponseEntity<ApiResponse<Void>> response = handler.handleGeneral(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getErrorCode()).isEqualTo(ErrorCode.INTERNAL_ERROR.getCode());
    }
}

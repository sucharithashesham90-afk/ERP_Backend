package com.erp.platform.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;
    private final HttpStatus httpStatus;

    public AppException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = resolveStatus(errorCode);
    }

    public static AppException notFound(String message) {
        return new AppException(ErrorCode.NOT_FOUND, message);
    }

    public static AppException conflict(String message) {
        return new AppException(ErrorCode.CONFLICT, message);
    }

    public static AppException forbidden(String message) {
        return new AppException(ErrorCode.FORBIDDEN, message);
    }

    public static AppException businessRule(String message) {
        return new AppException(ErrorCode.BUSINESS_RULE_VIOLATION, message);
    }

    public static AppException badRequest(String message) {
        return new AppException(ErrorCode.BUSINESS_RULE_VIOLATION, message);
    }

    public static AppException insufficientStock(String message) {
        return new AppException(ErrorCode.INSUFFICIENT_STOCK, message);
    }

    public static AppException accountPendingApproval(String message) {
        return new AppException(ErrorCode.ACCOUNT_PENDING_APPROVAL, message);
    }

    public static AppException accountRejected(String message) {
        return new AppException(ErrorCode.ACCOUNT_REJECTED, message);
    }

    public static AppException passwordExpired(String message) {
        return new AppException(ErrorCode.PASSWORD_EXPIRED, message);
    }

    private static HttpStatus resolveStatus(ErrorCode code) {
        return switch (code) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT, DUPLICATE_VALUE -> HttpStatus.CONFLICT;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN, ACCOUNT_PENDING_APPROVAL, ACCOUNT_REJECTED, PASSWORD_EXPIRED, MUST_CHANGE_PASSWORD ->
                    HttpStatus.FORBIDDEN;
            case VALIDATION_FAILED -> HttpStatus.BAD_REQUEST;
            case INSUFFICIENT_STOCK, BUSINESS_RULE_VIOLATION, INVALID_STATUS_TRANSITION ->
                    HttpStatus.UNPROCESSABLE_ENTITY;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}

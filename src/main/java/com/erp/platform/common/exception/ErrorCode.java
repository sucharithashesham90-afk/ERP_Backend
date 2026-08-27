package com.erp.platform.common.exception;

public enum ErrorCode {
    NOT_FOUND("ERR_001", "Resource not found"),
    DUPLICATE_VALUE("ERR_002", "Duplicate value"),
    INSUFFICIENT_STOCK("ERR_003", "Insufficient stock quantity"),
    INVALID_STATUS_TRANSITION("ERR_004", "Invalid status transition"),
    UNAUTHORIZED("ERR_005", "Unauthorized"),
    FORBIDDEN("ERR_006", "Access denied"),
    VALIDATION_FAILED("ERR_007", "Validation failed"),
    BUSINESS_RULE_VIOLATION("ERR_008", "Business rule violation"),
    CONFLICT("ERR_009", "Resource already exists"),
    INTERNAL_ERROR("ERR_010", "Internal server error"),
    ACCOUNT_PENDING_APPROVAL("ERR_011", "Account pending approval"),
    ACCOUNT_REJECTED("ERR_012", "Account rejected"),
    PASSWORD_EXPIRED("ERR_013", "Password expired"),
    MUST_CHANGE_PASSWORD("ERR_014", "Password change required");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() { return code; }
    public String getDefaultMessage() { return defaultMessage; }
}

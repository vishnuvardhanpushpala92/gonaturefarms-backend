package com.gonaturefarms.exception;

/**
 * Exception thrown when a duplicate field is detected during address creation/update.
 * This is used to provide specific field information to the frontend for better error handling.
 */
public class DuplicateFieldException extends RuntimeException {
    private final String field;
    private final String errorType;

    public DuplicateFieldException(String message, String field) {
        super(message);
        this.field = field;
        this.errorType = "DUPLICATE_FIELD";
    }

    public DuplicateFieldException(String message, String field, String errorType) {
        super(message);
        this.field = field;
        this.errorType = errorType;
    }

    public String getField() {
        return field;
    }

    public String getErrorType() {
        return errorType;
    }
}

package com.gonaturefarms.exception;

/**
 * Represents an expected, "soft" business-rule failure — e.g. "Phone already registered",
 * "Incorrect password", "Coupon code required". The original Express routes returned these
 * as {@code res.json({ success: false, message: '...' }) } with an implicit HTTP 200 status
 * (Express only changes status explicitly). To keep the frontend's response handling identical,
 * {@link com.gonaturefarms.exception.GlobalExceptionHandler} maps this exception to HTTP 200
 * with a {@code {success:false, message}} body rather than HTTP 400.
 */
public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}

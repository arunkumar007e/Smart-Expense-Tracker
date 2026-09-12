package com.expense.tracker.exception;

/** Thrown when a business/validation rule is violated (maps to HTTP 400). */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
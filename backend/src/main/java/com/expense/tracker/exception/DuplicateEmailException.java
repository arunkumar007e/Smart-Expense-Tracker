package com.expense.tracker.exception;

/** Thrown when a user tries to register with an email that is already in use (maps to HTTP 409). */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String message) {
        super(message);
    }
}
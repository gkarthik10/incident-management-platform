package com.karthik.incidentmanagement.exception;

/**
 * Thrown when login credentials are invalid.
 * Mapped to HTTP 401 by {@link GlobalExceptionHandler} — deliberately kept
 * separate from ResourceNotFoundException (404) so we never leak whether an
 * email exists in the system via the HTTP status code.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}

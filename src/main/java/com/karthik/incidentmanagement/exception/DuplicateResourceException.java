package com.karthik.incidentmanagement.exception;

/** Thrown when a create operation would violate a uniqueness constraint (e.g. email already registered). */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}

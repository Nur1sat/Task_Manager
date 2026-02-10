package com.taskmanager.domain.exceptions;

/**
 * Base domain exception.
 */
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}

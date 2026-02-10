package com.taskmanager.domain.exceptions;

/**
 * Thrown when a task transition violates domain rules.
 */
public class InvalidTaskStateException extends DomainException {
    public InvalidTaskStateException(String message) {
        super(message);
    }
}

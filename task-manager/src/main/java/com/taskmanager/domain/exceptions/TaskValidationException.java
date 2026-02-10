package com.taskmanager.domain.exceptions;

/**
 * Thrown when task input data is invalid.
 */
public class TaskValidationException extends DomainException {
    public TaskValidationException(String message) {
        super(message);
    }
}

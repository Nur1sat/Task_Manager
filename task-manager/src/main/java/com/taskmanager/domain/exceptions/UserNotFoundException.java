package com.taskmanager.domain.exceptions;

import java.util.UUID;

/**
 * Thrown when a user cannot be found.
 */
public class UserNotFoundException extends DomainException {
    public UserNotFoundException(UUID userId) {
        super("User not found: " + userId);
    }
}

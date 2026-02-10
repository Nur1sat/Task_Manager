package com.taskmanager.domain.exceptions;

import java.util.UUID;

/**
 * Thrown when a project cannot be found.
 */
public class ProjectNotFoundException extends DomainException {
    public ProjectNotFoundException(UUID projectId) {
        super("Project not found: " + projectId);
    }
}

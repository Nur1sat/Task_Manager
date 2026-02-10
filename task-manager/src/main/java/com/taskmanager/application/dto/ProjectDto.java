package com.taskmanager.application.dto;

import com.taskmanager.domain.model.Project;

import java.util.UUID;

/**
 * Project read model for UI adapters.
 */
public record ProjectDto(
        UUID id,
        String name,
        String description,
        UUID ownerUserId,
        boolean archived
) {
    public static ProjectDto fromDomain(Project project) {
        return new ProjectDto(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getOwnerUserId(),
                project.isArchived()
        );
    }
}

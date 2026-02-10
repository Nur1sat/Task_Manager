package com.taskmanager.infrastructure.persistence.mappers;

import com.taskmanager.domain.model.Project;
import com.taskmanager.infrastructure.persistence.entities.ProjectEntity;

/**
 * Maps between {@link Project} and {@link ProjectEntity}.
 */
public class ProjectEntityMapper {
    public ProjectEntity toEntity(Project project) {
        ProjectEntity entity = new ProjectEntity();
        entity.setId(project.getId());
        entity.setName(project.getName());
        entity.setDescription(project.getDescription());
        entity.setOwnerUserId(project.getOwnerUserId());
        entity.setArchived(project.isArchived());
        return entity;
    }

    public Project toDomain(ProjectEntity entity) {
        return new Project(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getOwnerUserId(),
                entity.isArchived()
        );
    }
}

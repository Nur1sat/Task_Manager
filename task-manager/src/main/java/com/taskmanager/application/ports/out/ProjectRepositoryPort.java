package com.taskmanager.application.ports.out;

import com.taskmanager.domain.model.Project;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for project persistence operations.
 */
public interface ProjectRepositoryPort {
    Project save(Project project);

    Optional<Project> findById(UUID projectId);

    List<Project> findAll();
}

package com.taskmanager.infrastructure.persistence.repositories;

import com.taskmanager.application.ports.out.ProjectRepositoryPort;
import com.taskmanager.domain.model.Project;
import com.taskmanager.infrastructure.persistence.entities.ProjectEntity;
import com.taskmanager.infrastructure.persistence.mappers.ProjectEntityMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA implementation of {@link ProjectRepositoryPort}.
 */
public class JpaProjectRepository implements ProjectRepositoryPort {
    private final JpaTransactionExecutor tx;
    private final ProjectEntityMapper mapper;

    public JpaProjectRepository(JpaTransactionExecutor tx, ProjectEntityMapper mapper) {
        this.tx = tx;
        this.mapper = mapper;
    }

    @Override
    public Project save(Project project) {
        return tx.write(em -> mapper.toDomain(em.merge(mapper.toEntity(project))));
    }

    @Override
    public Optional<Project> findById(UUID projectId) {
        return tx.read(em -> Optional.ofNullable(em.find(ProjectEntity.class, projectId)).map(mapper::toDomain));
    }

    @Override
    public List<Project> findAll() {
        return tx.read(em -> em.createQuery("select p from ProjectEntity p order by p.name", ProjectEntity.class)
                .getResultList()
                .stream()
                .map(mapper::toDomain)
                .toList());
    }
}

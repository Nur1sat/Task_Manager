package com.taskmanager.infrastructure.persistence.repositories;

import com.taskmanager.application.ports.out.TaskHistoryRepositoryPort;
import com.taskmanager.domain.model.TaskStatusHistory;
import com.taskmanager.infrastructure.persistence.entities.TaskHistoryEntity;
import com.taskmanager.infrastructure.persistence.mappers.TaskHistoryEntityMapper;

import java.util.List;
import java.util.UUID;

/**
 * JPA implementation of {@link TaskHistoryRepositoryPort}.
 */
public class JpaTaskHistoryRepository implements TaskHistoryRepositoryPort {
    private final JpaTransactionExecutor tx;
    private final TaskHistoryEntityMapper mapper;

    public JpaTaskHistoryRepository(JpaTransactionExecutor tx, TaskHistoryEntityMapper mapper) {
        this.tx = tx;
        this.mapper = mapper;
    }

    @Override
    public TaskStatusHistory save(TaskStatusHistory history) {
        return tx.write(em -> mapper.toDomain(em.merge(mapper.toEntity(history))));
    }

    @Override
    public List<TaskStatusHistory> findByTaskId(UUID taskId) {
        return tx.read(em -> em.createQuery(
                        "select h from TaskHistoryEntity h where h.taskId = :taskId order by h.changedAt desc",
                        TaskHistoryEntity.class)
                .setParameter("taskId", taskId)
                .getResultList()
                .stream()
                .map(mapper::toDomain)
                .toList());
    }
}

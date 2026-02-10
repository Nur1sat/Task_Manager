package com.taskmanager.application.ports.out;

import com.taskmanager.application.dto.SearchTasksQuery;
import com.taskmanager.domain.model.Task;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for task persistence operations.
 */
public interface TaskRepositoryPort {
    Task save(Task task);

    Optional<Task> findById(UUID taskId);

    List<Task> findAll(boolean includeArchived);

    List<Task> findByProjectId(UUID projectId, boolean includeArchived);

    List<Task> search(SearchTasksQuery query);

    Map<LocalDate, Long> countCompletedPerDay(LocalDate from, LocalDate to);

    long countOverdue(LocalDate onDate);

    double averageCompletionTimeDays();
}

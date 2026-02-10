package com.taskmanager.application.usecases;

import com.taskmanager.application.dto.TaskDto;
import com.taskmanager.application.dto.UpdateTaskCommand;
import com.taskmanager.application.ports.in.UpdateTaskInputPort;
import com.taskmanager.application.ports.out.TaskHistoryRepositoryPort;
import com.taskmanager.application.ports.out.TaskRepositoryPort;
import com.taskmanager.domain.exceptions.TaskNotFoundException;
import com.taskmanager.domain.exceptions.TaskValidationException;
import com.taskmanager.domain.model.Task;
import com.taskmanager.domain.model.TaskStatus;
import com.taskmanager.domain.model.TaskStatusHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Application service for updating tasks.
 */
public class UpdateTaskUseCase implements UpdateTaskInputPort {
    private static final Logger logger = LoggerFactory.getLogger(UpdateTaskUseCase.class);

    private final TaskRepositoryPort taskRepository;
    private final TaskHistoryRepositoryPort historyRepository;
    private final Clock clock;

    public UpdateTaskUseCase(TaskRepositoryPort taskRepository,
                             TaskHistoryRepositoryPort historyRepository,
                             Clock clock) {
        this.taskRepository = Objects.requireNonNull(taskRepository, "taskRepository is required");
        this.historyRepository = Objects.requireNonNull(historyRepository, "historyRepository is required");
        this.clock = Objects.requireNonNull(clock, "clock is required");
    }

    @Override
    public TaskDto execute(UpdateTaskCommand command) {
        validate(command);

        Task task = taskRepository.findById(command.taskId())
                .orElseThrow(() -> new TaskNotFoundException(command.taskId()));

        LocalDateTime now = LocalDateTime.now(clock);

        Task updated = task.update(
                command.title(),
                command.description(),
                command.priority(),
                command.dueDate(),
                command.tags(),
                command.blockingTaskIds(),
                now
        );

        TaskStatus previousStatus = task.getStatus();

        if (command.status() != null && command.status() != updated.getStatus()) {
            updated = updated.changeStatus(command.status(), now);
            recordHistory(updated.getId(), previousStatus, command.status(), now, null);
        }

        if (command.archive()) {
            TaskStatus statusBeforeArchive = updated.getStatus();
            updated = updated.archive(now);
            if (statusBeforeArchive != TaskStatus.ARCHIVED) {
                recordHistory(updated.getId(), statusBeforeArchive, TaskStatus.ARCHIVED, now, null);
            }
        }

        Task saved = taskRepository.save(updated);
        logger.info("Task updated: {}", saved.getId());
        return TaskDto.fromDomain(saved);
    }

    private void recordHistory(java.util.UUID taskId,
                               TaskStatus previous,
                               TaskStatus current,
                               LocalDateTime changedAt,
                               java.util.UUID changedBy) {
        TaskStatusHistory history = TaskStatusHistory.create(taskId, previous, current, changedAt, changedBy);
        historyRepository.save(history);
    }

    private void validate(UpdateTaskCommand command) {
        if (command == null) {
            throw new TaskValidationException("UpdateTaskCommand is required");
        }
        if (command.taskId() == null) {
            throw new TaskValidationException("taskId is required");
        }
    }
}

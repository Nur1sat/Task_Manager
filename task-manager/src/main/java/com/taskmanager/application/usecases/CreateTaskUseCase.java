package com.taskmanager.application.usecases;

import com.taskmanager.application.dto.CreateTaskCommand;
import com.taskmanager.application.dto.TaskDto;
import com.taskmanager.application.ports.in.CreateTaskInputPort;
import com.taskmanager.application.ports.out.ProjectRepositoryPort;
import com.taskmanager.application.ports.out.TaskRepositoryPort;
import com.taskmanager.application.ports.out.UserRepositoryPort;
import com.taskmanager.domain.exceptions.ProjectNotFoundException;
import com.taskmanager.domain.exceptions.TaskValidationException;
import com.taskmanager.domain.exceptions.UserNotFoundException;
import com.taskmanager.domain.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Application service for task creation.
 */
public class CreateTaskUseCase implements CreateTaskInputPort {
    private static final Logger logger = LoggerFactory.getLogger(CreateTaskUseCase.class);

    private final TaskRepositoryPort taskRepository;
    private final ProjectRepositoryPort projectRepository;
    private final UserRepositoryPort userRepository;
    private final Clock clock;

    public CreateTaskUseCase(TaskRepositoryPort taskRepository,
                             ProjectRepositoryPort projectRepository,
                             UserRepositoryPort userRepository,
                             Clock clock) {
        this.taskRepository = Objects.requireNonNull(taskRepository, "taskRepository is required");
        this.projectRepository = Objects.requireNonNull(projectRepository, "projectRepository is required");
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository is required");
        this.clock = Objects.requireNonNull(clock, "clock is required");
    }

    @Override
    public TaskDto execute(CreateTaskCommand command) {
        validate(command);

        projectRepository.findById(command.projectId())
                .orElseThrow(() -> new ProjectNotFoundException(command.projectId()));

        if (command.assigneeId() != null) {
            userRepository.findById(command.assigneeId())
                    .orElseThrow(() -> new UserNotFoundException(command.assigneeId()));
        }

        if (command.blockingTaskIds() != null) {
            for (java.util.UUID blockingId : command.blockingTaskIds()) {
                taskRepository.findById(blockingId).orElseThrow(
                        () -> new TaskValidationException("Blocking task not found: " + blockingId)
                );
            }
        }

        LocalDateTime now = LocalDateTime.now(clock);
        Task task = Task.create(
                command.projectId(),
                command.title(),
                command.description(),
                command.priority(),
                command.dueDate(),
                command.assigneeId(),
                command.tags(),
                command.blockingTaskIds(),
                now
        );

        Task saved = taskRepository.save(task);
        logger.info("Task created: {} for project {}", saved.getId(), saved.getProjectId());
        return TaskDto.fromDomain(saved);
    }

    private void validate(CreateTaskCommand command) {
        if (command == null) {
            throw new TaskValidationException("CreateTaskCommand is required");
        }
        if (command.projectId() == null) {
            throw new TaskValidationException("projectId is required");
        }
        if (command.title() == null || command.title().isBlank()) {
            throw new TaskValidationException("title is required");
        }
        if (command.dueDate() == null) {
            throw new TaskValidationException("dueDate is required");
        }
    }
}

package com.taskmanager.application.usecases;

import com.taskmanager.application.dto.ProjectDto;
import com.taskmanager.application.dto.TaskHistoryDto;
import com.taskmanager.application.dto.UserDto;
import com.taskmanager.application.ports.in.ReferenceDataInputPort;
import com.taskmanager.application.ports.out.ProjectRepositoryPort;
import com.taskmanager.application.ports.out.TaskHistoryRepositoryPort;
import com.taskmanager.application.ports.out.UserRepositoryPort;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Application service for reference data and task history.
 */
public class ReferenceDataUseCase implements ReferenceDataInputPort {
    private final ProjectRepositoryPort projectRepository;
    private final UserRepositoryPort userRepository;
    private final TaskHistoryRepositoryPort historyRepository;

    public ReferenceDataUseCase(ProjectRepositoryPort projectRepository,
                                UserRepositoryPort userRepository,
                                TaskHistoryRepositoryPort historyRepository) {
        this.projectRepository = Objects.requireNonNull(projectRepository, "projectRepository is required");
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository is required");
        this.historyRepository = Objects.requireNonNull(historyRepository, "historyRepository is required");
    }

    @Override
    public List<ProjectDto> getProjects() {
        return projectRepository.findAll().stream().map(ProjectDto::fromDomain).toList();
    }

    @Override
    public List<UserDto> getUsers() {
        return userRepository.findAll().stream().map(UserDto::fromDomain).toList();
    }

    @Override
    public List<TaskHistoryDto> getTaskHistory(UUID taskId) {
        return historyRepository.findByTaskId(taskId).stream().map(TaskHistoryDto::fromDomain).toList();
    }
}

package com.taskmanager.application.ports.in;

import com.taskmanager.application.dto.ProjectDto;
import com.taskmanager.application.dto.TaskHistoryDto;
import com.taskmanager.application.dto.UserDto;

import java.util.List;
import java.util.UUID;

/**
 * Input port for loading users, projects, and task history.
 */
public interface ReferenceDataInputPort {
    List<ProjectDto> getProjects();

    List<UserDto> getUsers();

    List<TaskHistoryDto> getTaskHistory(UUID taskId);
}

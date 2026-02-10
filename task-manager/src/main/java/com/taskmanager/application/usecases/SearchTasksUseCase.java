package com.taskmanager.application.usecases;

import com.taskmanager.application.dto.SearchTasksQuery;
import com.taskmanager.application.dto.TaskDto;
import com.taskmanager.application.ports.in.SearchTasksInputPort;
import com.taskmanager.application.ports.out.TaskRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * Application service for task search and filtering.
 */
public class SearchTasksUseCase implements SearchTasksInputPort {
    private static final Logger logger = LoggerFactory.getLogger(SearchTasksUseCase.class);

    private final TaskRepositoryPort taskRepository;

    public SearchTasksUseCase(TaskRepositoryPort taskRepository) {
        this.taskRepository = Objects.requireNonNull(taskRepository, "taskRepository is required");
    }

    @Override
    public List<TaskDto> execute(SearchTasksQuery query) {
        Objects.requireNonNull(query, "query is required");
        List<TaskDto> results = taskRepository.search(query)
                .stream()
                .map(TaskDto::fromDomain)
                .toList();

        logger.debug("Task search returned {} results", results.size());
        return results;
    }
}

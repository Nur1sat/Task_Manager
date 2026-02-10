package com.taskmanager.application.ports.in;

import com.taskmanager.application.dto.SearchTasksQuery;
import com.taskmanager.application.dto.TaskDto;

import java.util.List;

/**
 * Input port for searching tasks.
 */
public interface SearchTasksInputPort {
    List<TaskDto> execute(SearchTasksQuery query);
}

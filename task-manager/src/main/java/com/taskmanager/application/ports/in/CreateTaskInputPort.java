package com.taskmanager.application.ports.in;

import com.taskmanager.application.dto.CreateTaskCommand;
import com.taskmanager.application.dto.TaskDto;

/**
 * Input port for creating tasks.
 */
public interface CreateTaskInputPort {
    TaskDto execute(CreateTaskCommand command);
}

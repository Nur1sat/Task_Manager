package com.taskmanager.application.ports.in;

import com.taskmanager.application.dto.TaskDto;
import com.taskmanager.application.dto.UpdateTaskCommand;

/**
 * Input port for task updates.
 */
public interface UpdateTaskInputPort {
    TaskDto execute(UpdateTaskCommand command);
}

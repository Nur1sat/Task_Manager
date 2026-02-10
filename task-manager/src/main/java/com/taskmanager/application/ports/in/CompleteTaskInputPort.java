package com.taskmanager.application.ports.in;

import com.taskmanager.application.dto.CompleteTaskCommand;
import com.taskmanager.application.dto.TaskDto;

/**
 * Input port for task completion.
 */
public interface CompleteTaskInputPort {
    TaskDto execute(CompleteTaskCommand command);
}

package com.taskmanager.application.ports.in;

import com.taskmanager.application.dto.AssignTaskCommand;
import com.taskmanager.application.dto.TaskDto;

/**
 * Input port for task assignment.
 */
public interface AssignTaskInputPort {
    TaskDto execute(AssignTaskCommand command);
}

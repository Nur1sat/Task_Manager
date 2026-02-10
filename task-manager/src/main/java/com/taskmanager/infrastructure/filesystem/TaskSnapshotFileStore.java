package com.taskmanager.infrastructure.filesystem;

import com.taskmanager.domain.model.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Writes task snapshots to local files for lightweight operational backups.
 */
public class TaskSnapshotFileStore {
    public void writeSnapshot(Path filePath, List<Task> tasks) throws IOException {
        if (filePath.getParent() != null) {
            Files.createDirectories(filePath.getParent());
        }

        StringBuilder builder = new StringBuilder();
        for (Task task : tasks) {
            builder.append(task.getId())
                    .append('|')
                    .append(task.getTitle())
                    .append('|')
                    .append(task.getStatus())
                    .append('|')
                    .append(task.getDueDate())
                    .append(System.lineSeparator());
        }

        Files.writeString(filePath, builder.toString(), StandardCharsets.UTF_8);
    }
}

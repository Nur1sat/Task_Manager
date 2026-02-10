import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles task storage in memory and persistence on disk.
 */
public class TaskManager {
    private final List<Task> tasks = new ArrayList<>();
    private final Path storagePath;

    /**
     * Creates a manager that reads/writes tasks from the given file path.
     */
    public TaskManager(String filePath) {
        this.storagePath = Path.of(filePath);
    }

    /**
     * Adds a task to the list.
     */
    public void addTask(Task task) {
        if (task == null) {
            return;
        }
        tasks.add(task);
    }

    /**
     * Returns a read-only view for UI rendering.
     */
    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    /**
     * Marks the task at the given index as complete.
     */
    public void markTaskComplete(int index) {
        if (index >= 0 && index < tasks.size()) {
            tasks.get(index).markComplete();
        }
    }

    /**
     * Deletes the task at the given index.
     */
    public void deleteTask(int index) {
        if (index >= 0 && index < tasks.size()) {
            tasks.remove(index);
        }
    }

    /**
     * Saves all tasks to disk so they persist between runs.
     */
    public void saveTasks() throws IOException {
        if (storagePath.getParent() != null) {
            Files.createDirectories(storagePath.getParent());
        }

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(storagePath.toFile()))) {
            out.writeObject(new ArrayList<>(tasks));
        }
    }

    /**
     * Loads tasks from disk into memory. If no file exists, starts with an empty list.
     */
    @SuppressWarnings("unchecked")
    public void loadTasks() throws IOException, ClassNotFoundException {
        tasks.clear();
        if (!Files.exists(storagePath)) {
            return;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(storagePath.toFile()))) {
            Object loaded = in.readObject();
            if (loaded instanceof List<?>) {
                tasks.addAll((List<Task>) loaded);
            }
        }
    }
}

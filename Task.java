import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Represents one task item in the application.
 * This class is Serializable so tasks can be saved to disk.
 */
public class Task implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final String title;
    private final String description;
    private final LocalDate dueDate;
    private boolean completed;

    /**
     * Creates a new task with required data.
     */
    public Task(String title, String description, LocalDate dueDate) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty.");
        }
        if (dueDate == null) {
            throw new IllegalArgumentException("Due date is required.");
        }

        this.title = title.trim();
        this.description = description == null ? "" : description.trim();
        this.dueDate = dueDate;
        this.completed = false;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    /**
     * Marks this task as complete.
     */
    public void markComplete() {
        this.completed = true;
    }

    /**
     * Controls how the task appears in the Swing list.
     */
    @Override
    public String toString() {
        String status = completed ? "[Completed]" : "[Pending]";
        if (description.isEmpty()) {
            return status + " " + title + " (Due: " + dueDate.format(DATE_FORMAT) + ")";
        }
        return status + " " + title + " (Due: " + dueDate.format(DATE_FORMAT) + ") - " + description;
    }
}

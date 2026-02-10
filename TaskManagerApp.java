import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.DefaultListModel;

/**
 * Swing user interface for the TaskManager application.
 */
public class TaskManagerApp extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String STORAGE_FILE = "tasks.dat";

    private final TaskManager taskManager;
    private final DefaultListModel<Task> listModel;
    private final JList<Task> taskList;

    private final JTextField titleField;
    private final JTextArea descriptionArea;
    private final JSpinner dueDateSpinner;

    /**
     * Builds the window and initializes UI controls.
     */
    public TaskManagerApp() {
        super("TaskManager");

        taskManager = new TaskManager(STORAGE_FILE);
        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);
        titleField = new JTextField(22);
        descriptionArea = new JTextArea(4, 22);
        dueDateSpinner = new JSpinner(new SpinnerDateModel());

        initializeUI();
        loadTasksOnStartup();
    }

    /**
     * Main entry point that starts the Swing app on the EDT.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TaskManagerApp app = new TaskManagerApp();
            app.setVisible(true);
        });
    }

    /**
     * Creates and arranges all visible components.
     */
    private void initializeUI() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        add(buildInputPanel(), BorderLayout.NORTH);
        add(buildTaskListPanel(), BorderLayout.CENTER);
        add(buildActionPanel(), BorderLayout.SOUTH);

        setSize(760, 560);
        setLocationRelativeTo(null);

        // Save tasks when the user closes the app window.
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                saveTasksSilently();
            }
        });
    }

    /**
     * Top panel where users create new tasks.
     */
    private JPanel buildInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Create Task"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Title:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(titleField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Description:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        panel.add(new JScrollPane(descriptionArea), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        panel.add(new JLabel("Due Date:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dueDateSpinner, "yyyy-MM-dd");
        dueDateSpinner.setEditor(editor);
        panel.add(dueDateSpinner, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        JButton addButton = new JButton("Add Task");
        addButton.addActionListener(e -> addTaskFromInput());
        panel.add(addButton, gbc);

        return panel;
    }

    /**
     * Center panel that displays all tasks in a selectable list.
     */
    private JPanel buildTaskListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Task List"));

        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(taskList), BorderLayout.CENTER);

        return panel;
    }

    /**
     * Bottom panel with task actions.
     */
    private JPanel buildActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton completeButton = new JButton("Mark Complete");
        completeButton.addActionListener(e -> markSelectedTaskComplete());

        JButton deleteButton = new JButton("Delete Task");
        deleteButton.addActionListener(e -> deleteSelectedTask());

        JButton saveButton = new JButton("Save Now");
        saveButton.addActionListener(e -> saveTasksWithMessage());

        panel.add(completeButton);
        panel.add(deleteButton);
        panel.add(saveButton);

        return panel;
    }

    /**
     * Reads task data from the input controls and creates a new task.
     */
    private void addTaskFromInput() {
        try {
            String title = titleField.getText();
            String description = descriptionArea.getText();
            Date selectedDate = (Date) dueDateSpinner.getValue();
            LocalDate dueDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            Task task = new Task(title, description, dueDate);
            taskManager.addTask(task);
            refreshTaskList();
            saveTasksSilently();

            titleField.setText("");
            descriptionArea.setText("");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid Task", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Marks the selected task as complete.
     */
    private void markSelectedTaskComplete() {
        int index = taskList.getSelectedIndex();
        if (index < 0) {
            JOptionPane.showMessageDialog(this, "Select a task first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        taskManager.markTaskComplete(index);
        refreshTaskList();
        saveTasksSilently();
    }

    /**
     * Deletes the selected task.
     */
    private void deleteSelectedTask() {
        int index = taskList.getSelectedIndex();
        if (index < 0) {
            JOptionPane.showMessageDialog(this, "Select a task first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        taskManager.deleteTask(index);
        refreshTaskList();
        saveTasksSilently();
    }

    /**
     * Updates the visible list to match manager data.
     */
    private void refreshTaskList() {
        listModel.clear();
        for (Task task : taskManager.getTasks()) {
            listModel.addElement(task);
        }
    }

    /**
     * Loads tasks from file when the app starts.
     */
    private void loadTasksOnStartup() {
        try {
            taskManager.loadTasks();
            refreshTaskList();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not load tasks: " + ex.getMessage(), "Load Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Saves tasks and shows a success/error message.
     */
    private void saveTasksWithMessage() {
        try {
            taskManager.saveTasks();
            JOptionPane.showMessageDialog(this, "Tasks saved.", "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not save tasks: " + ex.getMessage(), "Save Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Saves tasks in the background flow without interrupting the user.
     */
    private void saveTasksSilently() {
        try {
            taskManager.saveTasks();
        } catch (Exception ignored) {
            // Silent save failure is ignored here; explicit save action shows errors to user.
        }
    }
}

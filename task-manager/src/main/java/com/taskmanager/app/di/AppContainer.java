package com.taskmanager.app.di;

import com.taskmanager.app.config.AppProperties;
import com.taskmanager.app.config.ExecutorFactory;
import com.taskmanager.app.ui.controllers.DashboardController;
import com.taskmanager.app.ui.controllers.TaskListController;
import com.taskmanager.app.ui.viewmodels.DashboardViewModel;
import com.taskmanager.app.ui.viewmodels.TaskDetailsViewModel;
import com.taskmanager.app.ui.viewmodels.TaskListViewModel;
import com.taskmanager.app.ui.views.MainView;
import com.taskmanager.application.ports.out.ProjectRepositoryPort;
import com.taskmanager.application.ports.out.TaskHistoryRepositoryPort;
import com.taskmanager.application.ports.out.TaskRepositoryPort;
import com.taskmanager.application.ports.out.UserRepositoryPort;
import com.taskmanager.application.usecases.AssignTaskUseCase;
import com.taskmanager.application.usecases.CompleteTaskUseCase;
import com.taskmanager.application.usecases.CreateTaskUseCase;
import com.taskmanager.application.usecases.ReferenceDataUseCase;
import com.taskmanager.application.usecases.SearchTasksUseCase;
import com.taskmanager.application.usecases.TaskAnalyticsUseCase;
import com.taskmanager.application.usecases.UpdateTaskUseCase;
import com.taskmanager.domain.model.Project;
import com.taskmanager.domain.model.User;
import com.taskmanager.infrastructure.config.ClockFactory;
import com.taskmanager.infrastructure.config.PersistenceManager;
import com.taskmanager.infrastructure.persistence.mappers.ProjectEntityMapper;
import com.taskmanager.infrastructure.persistence.mappers.TaskEntityMapper;
import com.taskmanager.infrastructure.persistence.mappers.TaskHistoryEntityMapper;
import com.taskmanager.infrastructure.persistence.mappers.UserEntityMapper;
import com.taskmanager.infrastructure.persistence.repositories.JpaProjectRepository;
import com.taskmanager.infrastructure.persistence.repositories.JpaTaskHistoryRepository;
import com.taskmanager.infrastructure.persistence.repositories.JpaTaskRepository;
import com.taskmanager.infrastructure.persistence.repositories.JpaTransactionExecutor;
import com.taskmanager.infrastructure.persistence.repositories.JpaUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * Manual dependency injection container for application startup.
 */
public class AppContainer {
    private static final Logger logger = LoggerFactory.getLogger(AppContainer.class);

    private final AppProperties appProperties;
    private final PersistenceManager persistenceManager;
    private final ExecutorService executorService;
    private final MainView mainView;

    public AppContainer() {
        this.appProperties = new AppProperties();
        this.executorService = new ExecutorFactory().fixedPool(appProperties.uiAsyncPoolSize());
        this.persistenceManager = new PersistenceManager(appProperties.toJpaOverrides());

        Clock clock = new ClockFactory().systemClock();

        JpaTransactionExecutor tx = new JpaTransactionExecutor(persistenceManager);

        UserRepositoryPort userRepository = new JpaUserRepository(tx, new UserEntityMapper());
        ProjectRepositoryPort projectRepository = new JpaProjectRepository(tx, new ProjectEntityMapper());
        TaskRepositoryPort taskRepository = new JpaTaskRepository(tx, new TaskEntityMapper());
        TaskHistoryRepositoryPort historyRepository = new JpaTaskHistoryRepository(tx, new TaskHistoryEntityMapper());

        CreateTaskUseCase createTask = new CreateTaskUseCase(taskRepository, projectRepository, userRepository, clock);
        UpdateTaskUseCase updateTask = new UpdateTaskUseCase(taskRepository, historyRepository, clock);
        CompleteTaskUseCase completeTask = new CompleteTaskUseCase(taskRepository, userRepository, historyRepository, clock);
        AssignTaskUseCase assignTask = new AssignTaskUseCase(taskRepository, userRepository, historyRepository, clock);
        SearchTasksUseCase searchTasks = new SearchTasksUseCase(taskRepository);
        TaskAnalyticsUseCase analytics = new TaskAnalyticsUseCase(taskRepository);
        ReferenceDataUseCase referenceData = new ReferenceDataUseCase(projectRepository, userRepository, historyRepository);

        TaskListViewModel taskListViewModel = new TaskListViewModel(
                createTask,
                updateTask,
                completeTask,
                assignTask,
                searchTasks,
                analytics,
                referenceData,
                executorService
        );

        DashboardViewModel dashboardViewModel = new DashboardViewModel();
        TaskDetailsViewModel detailsViewModel = new TaskDetailsViewModel();

        TaskListController taskListController = new TaskListController(taskListViewModel, detailsViewModel, dashboardViewModel);
        DashboardController dashboardController = new DashboardController(taskListViewModel, dashboardViewModel);

        this.mainView = new MainView(
                taskListViewModel,
                detailsViewModel,
                dashboardViewModel,
                taskListController,
                dashboardController
        );

        bootstrapReferenceData(userRepository, projectRepository);
    }

    public MainView mainView() {
        return mainView;
    }

    /**
     * Gracefully closes infrastructure resources.
     */
    public void close() {
        executorService.shutdown();
        persistenceManager.close();
    }

    private void bootstrapReferenceData(UserRepositoryPort userRepository, ProjectRepositoryPort projectRepository) {
        Objects.requireNonNull(userRepository, "userRepository is required");
        Objects.requireNonNull(projectRepository, "projectRepository is required");

        List<User> existingUsers = userRepository.findAll();
        if (existingUsers.isEmpty()) {
            User alice = userRepository.save(User.create("alice", "alice@taskmanager.local"));
            User bob = userRepository.save(User.create("bob", "bob@taskmanager.local"));
            logger.info("Seeded users: {}, {}", alice.getUsername(), bob.getUsername());
        }

        List<Project> existingProjects = projectRepository.findAll();
        if (existingProjects.isEmpty()) {
            User owner = userRepository.findAll().get(0);
            Project project = projectRepository.save(Project.create(
                    "Platform Reliability",
                    "Delivery board for reliability and maintenance initiatives",
                    owner.getId()
            ));
            logger.info("Seeded project: {}", project.getName());
        }
    }
}

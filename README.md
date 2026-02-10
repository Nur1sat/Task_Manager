# Task Management Repository

This repository contains two Java task-management applications:

1. `task-manager/` (primary): a production-style Java 17 desktop app built with JavaFX, Clean Architecture, and JPA/Hibernate.
2. Root-level Swing app (legacy): a lightweight single-window task tracker that persists to a local `tasks.dat` file.

If you are onboarding or contributing, start with `task-manager/`.

## Table of Contents

1. [Repository Overview](#repository-overview)
2. [Primary Application (`task-manager/`)](#primary-application-task-manager)
3. [Architecture and Design](#architecture-and-design)
4. [Domain Rules and Task Lifecycle](#domain-rules-and-task-lifecycle)
5. [User Interface Walkthrough](#user-interface-walkthrough)
6. [Persistence and Data Model](#persistence-and-data-model)
7. [Configuration Reference](#configuration-reference)
8. [Build, Run, and Test](#build-run-and-test)
9. [Legacy Swing Application (Root)](#legacy-swing-application-root)
10. [Troubleshooting](#troubleshooting)
11. [Development Notes and Extension Guide](#development-notes-and-extension-guide)
12. [License](#license)

## Repository Overview

### Why there are two apps

- The root-level Swing implementation is a compact, file-based task manager.
- The `task-manager/` module is a richer implementation with:
  - layered architecture (`domain`, `application`, `infrastructure`, `app`)
  - persistent storage via H2 + JPA/Hibernate
  - explicit use cases
  - dashboard analytics
  - status history tracking

### High-level feature comparison

| Capability | `task-manager/` (JavaFX, Maven) | Root Swing app |
|---|---|---|
| Multi-project support | Yes | No |
| Multi-user support | Yes (seeded users) | No |
| Task statuses | TODO, IN_PROGRESS, BLOCKED, COMPLETED, ARCHIVED | Pending/Completed flag |
| Task priority | LOW, MEDIUM, HIGH, CRITICAL | No |
| Tags | Yes | Free-text description only |
| Task dependencies | Yes (blocking task IDs) | No |
| Status history | Yes | No |
| Analytics | Yes | No |
| Persistence | H2 DB (file or in-memory) | Java serialization (`tasks.dat`) |
| Automated tests | Yes (JUnit 5) | No |

## Primary Application (`task-manager/`)

### Technology stack

- Java 17
- Maven
- JavaFX (`javafx-controls`, `javafx-fxml`)
- Hibernate ORM 6 + Jakarta Persistence
- H2 database
- SLF4J + Logback
- JUnit 5

### Primary packages

```text
task-manager/src/main/java/com/taskmanager
├── app            # JavaFX app entry point, DI container, UI controllers/viewmodels/views
├── application    # Use cases, DTOs, input/output ports
├── domain         # Pure domain model, exceptions, enums, value objects, domain events
└── infrastructure # JPA repositories/entities/mappers, persistence manager, logging, filesystem adapter
```

## Architecture and Design

### Layer boundaries (dependency direction)

- `domain`: no dependencies on other project layers.
- `application`: depends on `domain`.
- `infrastructure`: implements `application` output ports.
- `app`: composition root and UI layer; wires concrete adapters and use cases.

### Ports and adapters model

- Input ports (`application/ports/in`): represent use case APIs invoked by the UI.
- Output ports (`application/ports/out`): repository contracts for persistence.
- Adapters:
  - JavaFX controllers/viewmodels are input adapters.
  - JPA repositories are output adapters.

### Startup and wiring flow

`com.taskmanager.app.Main` creates `AppContainer`, which:

1. Loads app properties.
2. Creates async executor pool.
3. Builds JPA persistence manager and transaction executor.
4. Instantiates repository adapters.
5. Instantiates use case services.
6. Builds viewmodels/controllers/views.
7. Seeds reference data (users + project) if database is empty.

### Seeded reference data

On first run with an empty DB:

- Users:
  - `alice` (`alice@taskmanager.local`)
  - `bob` (`bob@taskmanager.local`)
- Project:
  - `Platform Reliability` (owned by the first seeded user)

## Domain Rules and Task Lifecycle

### Core domain objects

- `Task` (aggregate root): immutable; updates return a new instance.
- `Project`: immutable project aggregate.
- `User`: immutable user aggregate with `active` flag.
- `TaskStatusHistory`: immutable status transition record.
- `AuditInfo`: `createdAt` and `updatedAt`.

### Task fields

Each task includes:

- `id`, `projectId`
- `title`, `description`
- `priority`
- `status`
- `dueDate`
- `assigneeId` (nullable)
- `tags` (normalized to lowercase, unique)
- `blockingTaskIds` (UUID set, self-reference removed)
- `archived` flag
- audit timestamps
- `completedAt` timestamp when completed

### Invariants and validation

- `title`, `projectId`, `priority`, `status`, and `dueDate` are required.
- `completedAt` must be present if status is `COMPLETED`.
- `archived` must be `true` when status is `ARCHIVED`.
- Archived tasks cannot be modified.
- Blocking task IDs must exist at create time and be completed before completion.
- Assignee must exist and be active.

### Status transition behavior

Current transition constraints from domain logic:

- Transitioning from `ARCHIVED` is forbidden.
- `COMPLETED` can only transition to `ARCHIVED`.
- Reopening `COMPLETED` back to `TODO` is forbidden.
- Assigning a `TODO` task auto-advances it to `IN_PROGRESS`.
- Archiving is a soft delete (`status=ARCHIVED`, `archived=true`).

### Use cases

| Use case | Class | Summary |
|---|---|---|
| Create task | `CreateTaskUseCase` | Validates project/assignee/dependencies, creates TODO task |
| Update task | `UpdateTaskUseCase` | Updates mutable fields, optional status update, optional archive |
| Assign task | `AssignTaskUseCase` | Assigns user and moves TODO -> IN_PROGRESS |
| Complete task | `CompleteTaskUseCase` | Validates blockers and marks task completed |
| Search tasks | `SearchTasksUseCase` | Filters by project/status/priority/assignee/archive flag |
| Analytics | `TaskAnalyticsUseCase` | Completed-per-day, overdue count, average completion time |
| Reference data | `ReferenceDataUseCase` | Projects, users, and task status history |

### History recording

Status history entries are persisted when:

- Task status changes via `UpdateTaskUseCase`.
- Task is assigned from TODO to IN_PROGRESS.
- Task is completed.
- Task is archived.

## User Interface Walkthrough

The JavaFX UI (`MainView`) is split into three functional regions:

1. Top header:
   - Project selector
   - Dashboard metric cards
2. Left panel:
   - Filters (status, priority, assignee)
   - Task list
   - Actions (assign, complete, archive)
3. Right panel:
   - Selected task details + status history
   - Create task form

### Dashboard metrics

Dashboard displays:

- `Completed (7d)`: total completions over the last 7 days.
- `Overdue`: count of non-terminal, non-archived overdue tasks.
- `Avg Completion`: average completion duration in days.

### Create-task form specifics

Required workflow:

1. Select project.
2. Enter title and due date.
3. Optionally set description, priority, assignee, tags.
4. Optionally enter dependency IDs as comma-separated UUID values.

If dependency UUID format is invalid, creation is blocked and an error dialog is shown.

## Persistence and Data Model

### Storage mode

H2 is used through JPA/Hibernate with `RESOURCE_LOCAL` transactions.

- File mode URL pattern:
  - `jdbc:h2:file:<app.db.file.path>;AUTO_SERVER=TRUE;MODE=LEGACY`
- Memory mode URL pattern:
  - `jdbc:h2:mem:<app.db.inmemory.name>;DB_CLOSE_DELAY=-1;MODE=LEGACY`

### Main tables/entities

- `users` (`UserEntity`)
- `projects` (`ProjectEntity`)
- `tasks` (`TaskEntity`)
- `task_history` (`TaskHistoryEntity`)
- `task_tags` (element collection for tags)
- `task_dependencies` (element collection for blocking task IDs)

### Query behavior

Task queries are ordered by:

1. `dueDate` ascending
2. `updatedAt` descending

Search filters currently support:

- `projectId`
- `status`
- `priority`
- `assigneeId`
- `includeArchived`

## Configuration Reference

File: `task-manager/src/main/resources/application.properties`

| Property | Default | Meaning |
|---|---|---|
| `app.db.mode` | `file` | DB mode (`file`, `memory`, or `in-memory`) |
| `app.db.file.path` | `./data/taskdb` | File-based H2 path (relative to app working directory) |
| `app.db.inmemory.name` | `taskdb` | In-memory H2 DB name |
| `app.db.username` | `sa` | H2 username |
| `app.db.password` | *(empty)* | H2 password |
| `app.hibernate.hbm2ddl` | `update` | Hibernate schema strategy |
| `app.ui.async.pool.size` | `4` | Requested async worker pool size (minimum actual size is 2) |

### Logging

File: `task-manager/src/main/resources/logback.xml`

- Root log level: `INFO`
- Output: console
- Hibernate SQL and bind logs: `WARN`

## Build, Run, and Test

Run all commands from `task-manager/` unless noted otherwise.

### Prerequisites

- JDK 17+
- Maven 3.9+

### Install dependencies and compile

```bash
cd task-manager
mvn clean compile
```

### Run JavaFX desktop application

```bash
cd task-manager
mvn javafx:run
```

### Run tests

```bash
cd task-manager
mvn test
```

### Main test coverage today

`TaskUseCaseTest` validates key behaviors:

- task creation persistence flow
- completion blocked by unresolved dependencies
- analytics aggregation values

## Legacy Swing Application (Root)

Root-level files:

- `Task.java`
- `TaskManager.java`
- `TaskManagerApp.java`

This is a simple Swing app with serialized local persistence.

### Behavior

- Add tasks with title, description, due date
- Mark complete
- Delete tasks
- Save/load from `tasks.dat` in the current directory

### Compile and run

From repository root:

```bash
javac Task.java TaskManager.java TaskManagerApp.java
java TaskManagerApp
```

## Troubleshooting

### JavaFX runtime issues

Symptom:
- App fails to start with JavaFX class/module errors.

Actions:
1. Confirm Java 17+ is active (`java -version`).
2. Run via Maven plugin (`mvn javafx:run`) from `task-manager/` instead of direct `java`.

### Database lock or startup issues in file mode

Symptom:
- H2 file open/lock errors.

Actions:
1. Ensure no stale app process is running.
2. Remove local DB files under `task-manager/data/` if you want a clean reset.
3. Switch to memory mode by setting `app.db.mode=memory` for isolated runs.

### Task completion blocked unexpectedly

Symptom:
- Completing a task fails with blocker-related error.

Actions:
1. Open task details and inspect dependency IDs.
2. Complete all blocking tasks first.
3. Retry completion.

### Invalid dependency format

Symptom:
- Create task fails when dependencies are entered.

Action:
- Ensure dependency input is a comma-separated list of valid UUIDs only.

## Development Notes and Extension Guide

### Common extension workflow

When adding a new business operation:

1. Add or update domain behavior in `domain/`.
2. Define input port method in `application/ports/in`.
3. Implement use case in `application/usecases`.
4. Add/adjust output port contract if persistence behavior changes.
5. Implement adapter changes in `infrastructure/`.
6. Wire dependencies in `AppContainer`.
7. Add UI/viewmodel/controller interaction if needed.
8. Add or update tests.

### Areas that are intentionally simple today

- No authentication or user login flow.
- No UI for creating/editing users or projects (reference data is seeded).
- No REST API (desktop-only delivery adapter).
- No migration toolchain (schema relies on Hibernate `hbm2ddl` strategy).

### Notes on currently unused building blocks

- `domain/events/*` and value-object wrappers (`TaskId`, `ProjectId`, `UserId`, `Tag`) exist and can support stricter modeling or event publication in future iterations.
- `infrastructure/filesystem/TaskSnapshotFileStore` can write lightweight task snapshots but is not wired into startup flow by default.

## License

MIT License. See `LICENSE` for full text.

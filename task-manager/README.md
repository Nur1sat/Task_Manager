# Task Manager

Production-grade Java 17 task management system using Clean Architecture with Hexagonal Ports & Adapters.

## Tech Stack

- Java 17+
- Maven
- JavaFX (desktop UI)
- H2 + JPA/Hibernate
- SLF4J + Logback
- JUnit 5

## Architecture

The codebase is organized by architectural layers under `src/main/java/com/taskmanager`:

- `app`
  - UI composition root, JavaFX controllers/views/viewmodels, app config, manual DI.
- `domain`
  - Pure domain model, value objects, domain events, domain exceptions.
- `application`
  - Use case orchestration, DTOs, input/output ports.
- `infrastructure`
  - JPA entities, repository adapters, mappers, persistence config, filesystem and logging adapters.

Dependency direction:

- `domain` depends on nothing else.
- `application` depends on `domain` only.
- `infrastructure` implements `application` outbound ports.
- `app` wires concrete implementations and JavaFX delivery concerns.

## Functional Coverage

- Multi-project, multi-user support
- Task model: title, description, priority, status, due date, assignee, tags, audit info
- Task dependencies (blocking task IDs)
- Soft delete (archived tasks)
- Task status history log
- Search and filtering by status, priority, assignee, project
- Analytics:
  - completed tasks per day
  - overdue tasks count
  - average completion time

## Project Layout

```text
task-manager
├── pom.xml
├── README.md
├── src
│   ├── main
│   │   ├── java/com/taskmanager
│   │   │   ├── app
│   │   │   ├── domain
│   │   │   ├── application
│   │   │   └── infrastructure
│   │   └── resources
│   │       ├── application.properties
│   │       ├── logback.xml
│   │       ├── styles.css
│   │       └── META-INF/persistence.xml
│   └── test
│       └── java/com/taskmanager/application/usecases/TaskUseCaseTest.java
```

## Configuration

`src/main/resources/application.properties`:

- `app.db.mode=file|memory`
- `app.db.file.path=./data/taskdb`
- `app.db.inmemory.name=taskdb`
- `app.db.username=sa`
- `app.db.password=`
- `app.hibernate.hbm2ddl=update`
- `app.ui.async.pool.size=4`

## Run

```bash
mvn clean javafx:run
```

## Test

```bash
mvn test
```

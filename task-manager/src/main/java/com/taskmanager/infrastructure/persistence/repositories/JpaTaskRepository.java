package com.taskmanager.infrastructure.persistence.repositories;

import com.taskmanager.application.dto.SearchTasksQuery;
import com.taskmanager.application.ports.out.TaskRepositoryPort;
import com.taskmanager.domain.model.Task;
import com.taskmanager.domain.model.TaskStatus;
import com.taskmanager.infrastructure.persistence.entities.TaskEntity;
import com.taskmanager.infrastructure.persistence.mappers.TaskEntityMapper;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA implementation of {@link TaskRepositoryPort}.
 */
public class JpaTaskRepository implements TaskRepositoryPort {
    private final JpaTransactionExecutor tx;
    private final TaskEntityMapper mapper;

    public JpaTaskRepository(JpaTransactionExecutor tx, TaskEntityMapper mapper) {
        this.tx = tx;
        this.mapper = mapper;
    }

    @Override
    public Task save(Task task) {
        return tx.write(em -> mapper.toDomain(em.merge(mapper.toEntity(task))));
    }

    @Override
    public Optional<Task> findById(UUID taskId) {
        return tx.read(em -> Optional.ofNullable(em.find(TaskEntity.class, taskId)).map(mapper::toDomain));
    }

    @Override
    public List<Task> findAll(boolean includeArchived) {
        return tx.read(em -> em.createQuery(
                        "select t from TaskEntity t where (:includeArchived = true or t.archived = false) " +
                                "order by t.dueDate asc, t.updatedAt desc", TaskEntity.class)
                .setParameter("includeArchived", includeArchived)
                .getResultList()
                .stream()
                .map(mapper::toDomain)
                .toList());
    }

    @Override
    public List<Task> findByProjectId(UUID projectId, boolean includeArchived) {
        return tx.read(em -> em.createQuery(
                        "select t from TaskEntity t where t.projectId = :projectId and " +
                                "(:includeArchived = true or t.archived = false) order by t.dueDate asc, t.updatedAt desc",
                        TaskEntity.class)
                .setParameter("projectId", projectId)
                .setParameter("includeArchived", includeArchived)
                .getResultList()
                .stream()
                .map(mapper::toDomain)
                .toList());
    }

    @Override
    public List<Task> search(SearchTasksQuery query) {
        return tx.read(em -> {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<TaskEntity> cq = cb.createQuery(TaskEntity.class);
            Root<TaskEntity> root = cq.from(TaskEntity.class);

            List<Predicate> predicates = new ArrayList<>();
            if (query.projectId() != null) {
                predicates.add(cb.equal(root.get("projectId"), query.projectId()));
            }
            if (query.status() != null) {
                predicates.add(cb.equal(root.get("status"), query.status()));
            }
            if (query.priority() != null) {
                predicates.add(cb.equal(root.get("priority"), query.priority()));
            }
            if (query.assigneeId() != null) {
                predicates.add(cb.equal(root.get("assigneeId"), query.assigneeId()));
            }
            if (!query.includeArchived()) {
                predicates.add(cb.isFalse(root.get("archived")));
            }

            cq.select(root)
                    .where(predicates.toArray(Predicate[]::new))
                    .orderBy(cb.asc(root.get("dueDate")), cb.desc(root.get("updatedAt")));

            return em.createQuery(cq)
                    .getResultList()
                    .stream()
                    .map(mapper::toDomain)
                    .toList();
        });
    }

    @Override
    public Map<LocalDate, Long> countCompletedPerDay(LocalDate from, LocalDate to) {
        LocalDateTime fromTs = from.atStartOfDay();
        LocalDateTime toTs = to.plusDays(1).atStartOfDay();

        return tx.read(em -> {
            List<LocalDateTime> completionTimes = em.createQuery(
                            "select t.completedAt from TaskEntity t where t.completedAt is not null " +
                                    "and t.completedAt >= :fromTs and t.completedAt < :toTs",
                            LocalDateTime.class)
                    .setParameter("fromTs", fromTs)
                    .setParameter("toTs", toTs)
                    .getResultList();

            Map<LocalDate, Long> grouped = new LinkedHashMap<>();
            LocalDate day = from;
            while (!day.isAfter(to)) {
                grouped.put(day, 0L);
                day = day.plusDays(1);
            }

            for (LocalDateTime time : completionTimes) {
                LocalDate date = time.toLocalDate();
                grouped.computeIfPresent(date, (k, v) -> v + 1);
            }
            return grouped;
        });
    }

    @Override
    public long countOverdue(LocalDate onDate) {
        return tx.read(em -> em.createQuery(
                        "select count(t) from TaskEntity t where t.archived = false and t.status not in :terminalStatuses " +
                                "and t.dueDate < :onDate",
                        Long.class)
                .setParameter("terminalStatuses", List.of(TaskStatus.COMPLETED, TaskStatus.ARCHIVED))
                .setParameter("onDate", onDate)
                .getSingleResult());
    }

    @Override
    public double averageCompletionTimeDays() {
        return tx.read(em -> {
            List<Object[]> tuples = em.createQuery(
                            "select t.createdAt, t.completedAt from TaskEntity t where t.completedAt is not null",
                            Object[].class)
                    .getResultList();
            if (tuples.isEmpty()) {
                return 0.0;
            }

            double totalDays = 0.0;
            for (Object[] tuple : tuples) {
                LocalDateTime createdAt = (LocalDateTime) tuple[0];
                LocalDateTime completedAt = (LocalDateTime) tuple[1];
                totalDays += Duration.between(createdAt, completedAt).toHours() / 24.0;
            }
            return totalDays / tuples.size();
        });
    }
}

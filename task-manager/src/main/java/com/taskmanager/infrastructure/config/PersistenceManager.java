package com.taskmanager.infrastructure.config;

import com.taskmanager.infrastructure.persistence.entities.ProjectEntity;
import com.taskmanager.infrastructure.persistence.entities.TaskEntity;
import com.taskmanager.infrastructure.persistence.entities.TaskHistoryEntity;
import com.taskmanager.infrastructure.persistence.entities.UserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Owns the JPA EntityManagerFactory lifecycle.
 */
public class PersistenceManager {
    private static final String PERSISTENCE_UNIT = "task-manager-pu";

    private final EntityManagerFactory entityManagerFactory;

    public PersistenceManager(Map<String, String> overrides) {
        Map<String, String> properties = new HashMap<>(Objects.requireNonNull(overrides, "overrides are required"));

        properties.putIfAbsent("jakarta.persistence.jdbc.driver", "org.h2.Driver");
        properties.putIfAbsent("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.putIfAbsent("hibernate.show_sql", "false");
        properties.putIfAbsent("hibernate.format_sql", "false");
        properties.putIfAbsent("hibernate.hbm2ddl.auto", "update");

        this.entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, properties);
        forceEntityDiscovery();
    }

    public EntityManagerFactory entityManagerFactory() {
        return entityManagerFactory;
    }

    public EntityManager newEntityManager() {
        return entityManagerFactory.createEntityManager();
    }

    public void close() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

    private void forceEntityDiscovery() {
        Class<?>[] classes = {
                TaskEntity.class,
                ProjectEntity.class,
                UserEntity.class,
                TaskHistoryEntity.class
        };
        for (Class<?> ignored : classes) {
            // No-op. Reference classes explicitly so they stay visible in native-image or aggressive packaging scenarios.
        }
    }
}

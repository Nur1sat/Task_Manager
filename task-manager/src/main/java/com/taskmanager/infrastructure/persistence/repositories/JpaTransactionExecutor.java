package com.taskmanager.infrastructure.persistence.repositories;

import com.taskmanager.infrastructure.config.PersistenceManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.Objects;
import java.util.function.Function;

/**
 * Utility for wrapping JPA operations in consistent transaction boundaries.
 */
public class JpaTransactionExecutor {
    private final PersistenceManager persistenceManager;

    public JpaTransactionExecutor(PersistenceManager persistenceManager) {
        this.persistenceManager = Objects.requireNonNull(persistenceManager, "persistenceManager is required");
    }

    public <T> T read(Function<EntityManager, T> operation) {
        EntityManager em = persistenceManager.newEntityManager();
        try {
            return operation.apply(em);
        } finally {
            em.close();
        }
    }

    public <T> T write(Function<EntityManager, T> operation) {
        EntityManager em = persistenceManager.newEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T result = operation.apply(em);
            tx.commit();
            return result;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }
}

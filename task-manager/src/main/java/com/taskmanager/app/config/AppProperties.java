package com.taskmanager.app.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Reads application configuration from classpath application.properties.
 */
public class AppProperties {
    private final Properties properties;

    public AppProperties() {
        this.properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IllegalStateException("application.properties was not found on classpath");
            }
            this.properties.load(input);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load application.properties", ex);
        }
    }

    public String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        String raw = properties.getProperty(key);
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        return Integer.parseInt(raw.trim());
    }

    public String dbMode() {
        return get("app.db.mode", "file").trim().toLowerCase();
    }

    public String dbFilePath() {
        return get("app.db.file.path", "./data/taskdb");
    }

    public String dbInMemoryName() {
        return get("app.db.inmemory.name", "taskdb");
    }

    public String dbUsername() {
        return get("app.db.username", "sa");
    }

    public String dbPassword() {
        return get("app.db.password", "");
    }

    public String ddlMode() {
        return get("app.hibernate.hbm2ddl", "update");
    }

    public int uiAsyncPoolSize() {
        return getInt("app.ui.async.pool.size", 4);
    }

    /**
     * Builds a property map for JPA runtime overrides.
     */
    public Map<String, String> toJpaOverrides() {
        Map<String, String> map = new HashMap<>();
        map.put("jakarta.persistence.jdbc.url", buildJdbcUrl());
        map.put("jakarta.persistence.jdbc.user", dbUsername());
        map.put("jakarta.persistence.jdbc.password", dbPassword());
        map.put("hibernate.hbm2ddl.auto", ddlMode());
        return map;
    }

    private String buildJdbcUrl() {
        if (Objects.equals(dbMode(), "memory") || Objects.equals(dbMode(), "in-memory")) {
            return "jdbc:h2:mem:" + dbInMemoryName() + ";DB_CLOSE_DELAY=-1;MODE=LEGACY";
        }
        return "jdbc:h2:file:" + dbFilePath() + ";AUTO_SERVER=TRUE;MODE=LEGACY";
    }
}

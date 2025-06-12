package com.fpt.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:fpt.db";
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            initializeDatabase();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }

    private void initializeDatabase() {
        try (Statement stmt = connection.createStatement()) {
            // Создаем таблицу проектов
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS projects (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    description TEXT,
                    client TEXT,
                    status TEXT NOT NULL,
                    start_date DATE,
                    deadline DATE,
                    budget REAL,
                    hourly_rate REAL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Проверяем наличие колонки hourly_rate
            try {
                stmt.execute("SELECT hourly_rate FROM projects LIMIT 1");
            } catch (SQLException e) {
                // Если колонки нет, добавляем её
                stmt.execute("ALTER TABLE projects ADD COLUMN hourly_rate REAL");
            }

            // Создаем таблицу временных записей
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS time_entries (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    project_id INTEGER,
                    start_time TIMESTAMP NOT NULL,
                    end_time TIMESTAMP,
                    description TEXT,
                    FOREIGN KEY (project_id) REFERENCES projects(id)
                )
            """);

            // Создаем таблицу для мониторинга нагрузки
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS workload_alerts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date DATE NOT NULL,
                    total_hours REAL NOT NULL,
                    alert_level TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database tables", e);
        }
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // Игнорируем ошибку закрытия
            }
        }
    }
} 
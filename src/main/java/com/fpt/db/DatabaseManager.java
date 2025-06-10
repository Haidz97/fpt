package com.fpt.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DatabaseManager {
    private static final String DB_NAME = "fpt.db";
    private static final String DB_PATH = System.getProperty("user.home") + "/.fpt/" + DB_NAME;
    private static DatabaseManager instance;
    private Connection connection;

    static {
        try {
            // Явная загрузка драйвера SQLite
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }
    }

    private DatabaseManager() {
        initializeDatabase();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void initializeDatabase() {
        try {
            // Создаем директорию для базы данных, если её нет
            Path dbDir = Paths.get(System.getProperty("user.home"), ".fpt");
            if (!dbDir.toFile().exists()) {
                dbDir.toFile().mkdirs();
            }

            // Подключаемся к базе данных (создастся, если не существует)
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);

            // Создаем таблицы
            createTables();

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private void createTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            // Таблица проектов
            statement.execute("""
                CREATE TABLE IF NOT EXISTS projects (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    description TEXT,
                    client TEXT,
                    status TEXT NOT NULL,
                    start_date TEXT NOT NULL,
                    deadline TEXT,
                    budget REAL,
                    hourly_rate REAL,
                    time_spent INTEGER,
                    actual_income REAL,
                    source TEXT,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    updated_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Таблица для трекинга времени
            statement.execute("""
                CREATE TABLE IF NOT EXISTS time_entries (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    project_id INTEGER NOT NULL,
                    start_time TEXT NOT NULL,
                    end_time TEXT,
                    description TEXT,
                    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
                )
            """);

            // Таблица для категорий проектов
            statement.execute("""
                CREATE TABLE IF NOT EXISTS project_categories (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE
                )
            """);

            // Связь между проектами и категориями
            statement.execute("""
                CREATE TABLE IF NOT EXISTS project_category_relations (
                    project_id INTEGER NOT NULL,
                    category_id INTEGER NOT NULL,
                    PRIMARY KEY (project_id, category_id),
                    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
                    FOREIGN KEY (category_id) REFERENCES project_categories(id) ON DELETE CASCADE
                )
            """);

            // Таблица для настроек приложения
            statement.execute("""
                CREATE TABLE IF NOT EXISTS settings (
                    key TEXT PRIMARY KEY,
                    value TEXT NOT NULL
                )
            """);

            // Таблица для API ключей (с шифрованием)
            statement.execute("""
                CREATE TABLE IF NOT EXISTS api_keys (
                    service TEXT PRIMARY KEY,
                    encrypted_key TEXT NOT NULL,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
            """);
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                initializeDatabase();
            }
            return connection;
        } catch (SQLException e) {
            System.err.println("Error getting database connection: " + e.getMessage());
            throw new RuntimeException("Failed to get database connection", e);
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}
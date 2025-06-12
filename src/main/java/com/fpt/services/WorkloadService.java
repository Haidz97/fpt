package com.fpt.services;

import com.fpt.models.Project;
import com.fpt.models.ProjectStatus;
import com.fpt.models.TimeEntry;
import com.fpt.models.WorkloadSettings;
import com.fpt.utils.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Сервис для управления настройками нагрузки и проверки соблюдения ограничений.
 * Отвечает за:
 * - Сохранение и загрузку настроек нагрузки
 * - Проверку превышения дневной и недельной нагрузки
 * - Обнаружение конфликтов в расписании
 * - Отслеживание приближающихся дедлайнов
 */
public class WorkloadService {
    private final DatabaseManager dbManager;
    private WorkloadSettings settings;

    /**
     * Создает новый экземпляр сервиса.
     * При создании проверяет наличие таблицы настроек и загружает текущие настройки.
     *
     * @param dbManager менеджер базы данных
     */
    public WorkloadService(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        createWorkloadSettingsTable();
        loadSettings();
    }

    /**
     * Создает таблицу настроек в базе данных, если она еще не существует.
     * Таблица содержит все параметры из класса WorkloadSettings.
     */
    private void createWorkloadSettingsTable() {
        try (Statement stmt = dbManager.getConnection().createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS workload_settings (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    max_daily_hours REAL NOT NULL,
                    max_weekly_hours REAL NOT NULL,
                    desired_project_count INTEGER NOT NULL,
                    enable_deadline_notifications BOOLEAN NOT NULL,
                    enable_workload_notifications BOOLEAN NOT NULL,
                    enable_schedule_conflict_notifications BOOLEAN NOT NULL,
                    deadline_warning_days INTEGER NOT NULL
                )
            """);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create workload_settings table", e);
        }
    }

    /**
     * Загружает настройки из базы данных.
     * Если настройки не найдены, создает новые с значениями по умолчанию.
     */
    private void loadSettings() {
        String sql = "SELECT * FROM workload_settings LIMIT 1";
        try (Statement stmt = dbManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                settings = new WorkloadSettings();
                settings.setId(rs.getLong("id"));
                settings.setMaxDailyHours(rs.getDouble("max_daily_hours"));
                settings.setMaxWeeklyHours(rs.getDouble("max_weekly_hours"));
                settings.setDesiredProjectCount(rs.getInt("desired_project_count"));
                settings.setEnableDeadlineNotifications(rs.getBoolean("enable_deadline_notifications"));
                settings.setEnableWorkloadNotifications(rs.getBoolean("enable_workload_notifications"));
                settings.setEnableScheduleConflictNotifications(rs.getBoolean("enable_schedule_conflict_notifications"));
                settings.setDeadlineWarningDays(rs.getInt("deadline_warning_days"));
            } else {
                // Если настройки не найдены, создаем настройки по умолчанию
                settings = new WorkloadSettings();
                saveSettings(settings);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load workload settings", e);
        }
    }

    /**
     * Сохраняет настройки в базу данных.
     * Если настройки новые (id == null), создает новую запись.
     * Иначе обновляет существующую запись.
     *
     * @param settings настройки для сохранения
     */
    public void saveSettings(WorkloadSettings settings) {
        if (settings.getId() == null) {
            String sql = """
                INSERT INTO workload_settings (
                    max_daily_hours, max_weekly_hours, desired_project_count,
                    enable_deadline_notifications, enable_workload_notifications,
                    enable_schedule_conflict_notifications, deadline_warning_days
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
            try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
                stmt.setDouble(1, settings.getMaxDailyHours());
                stmt.setDouble(2, settings.getMaxWeeklyHours());
                stmt.setInt(3, settings.getDesiredProjectCount());
                stmt.setBoolean(4, settings.isEnableDeadlineNotifications());
                stmt.setBoolean(5, settings.isEnableWorkloadNotifications());
                stmt.setBoolean(6, settings.isEnableScheduleConflictNotifications());
                stmt.setInt(7, settings.getDeadlineWarningDays());
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        settings.setId(rs.getLong(1));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to save workload settings", e);
            }
        } else {
            String sql = """
                UPDATE workload_settings SET
                    max_daily_hours = ?,
                    max_weekly_hours = ?,
                    desired_project_count = ?,
                    enable_deadline_notifications = ?,
                    enable_workload_notifications = ?,
                    enable_schedule_conflict_notifications = ?,
                    deadline_warning_days = ?
                WHERE id = ?
            """;
            try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
                stmt.setDouble(1, settings.getMaxDailyHours());
                stmt.setDouble(2, settings.getMaxWeeklyHours());
                stmt.setInt(3, settings.getDesiredProjectCount());
                stmt.setBoolean(4, settings.isEnableDeadlineNotifications());
                stmt.setBoolean(5, settings.isEnableWorkloadNotifications());
                stmt.setBoolean(6, settings.isEnableScheduleConflictNotifications());
                stmt.setInt(7, settings.getDeadlineWarningDays());
                stmt.setLong(8, settings.getId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to update workload settings", e);
            }
        }
        this.settings = settings;
    }

    /**
     * Возвращает текущие настройки нагрузки.
     *
     * @return текущие настройки
     */
    public WorkloadSettings getSettings() {
        return settings;
    }

    /**
     * Проверяет соблюдение всех ограничений нагрузки и возвращает список проблем.
     * Проверяет:
     * - Количество активных проектов
     * - Дневную нагрузку
     * - Недельную нагрузку
     * - Конфликты в расписании
     * - Приближающиеся дедлайны
     *
     * @param projects список всех проектов
     * @param timeEntries список всех временных записей
     * @return список обнаруженных проблем в виде текстовых сообщений
     */
    public List<String> checkWorkloadIssues(List<Project> projects, List<TimeEntry> timeEntries) {
        List<String> issues = new ArrayList<>();
        
        // Проверяем количество активных проектов
        long activeProjectCount = projects.stream()
                .filter(p -> p.getStatus() == ProjectStatus.IN_PROGRESS)
                .count();
        if (activeProjectCount > settings.getDesiredProjectCount()) {
            issues.add(String.format(
                "Превышено желаемое количество проектов: %d (максимум: %d)",
                activeProjectCount, settings.getDesiredProjectCount()
            ));
        }

        // Группируем записи времени по дням для удобства проверки
        Map<LocalDate, Double> dailyHours = new HashMap<>();
        Map<LocalDate, List<TimeEntry>> dailyEntries = new HashMap<>();

        for (TimeEntry entry : timeEntries) {
            if (entry.getEndTime() != null) {
                LocalDate date = entry.getStartTime().toLocalDate();
                dailyHours.merge(date, entry.getDurationInHours(), Double::sum);
                dailyEntries.computeIfAbsent(date, k -> new ArrayList<>()).add(entry);
            }
        }

        // Проверяем дневную нагрузку
        dailyHours.forEach((date, hours) -> {
            if (hours > settings.getMaxDailyHours()) {
                issues.add(String.format(
                    "Превышена дневная нагрузка %s: %.1f часов (максимум: %.1f)",
                    date, hours, settings.getMaxDailyHours()
                ));
            }
        });

        // Проверяем недельную нагрузку
        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.minusDays(now.getDayOfWeek().getValue() - 1);
        double weeklyHours = dailyHours.entrySet().stream()
                .filter(e -> !e.getKey().isBefore(weekStart))
                .mapToDouble(Map.Entry::getValue)
                .sum();

        if (weeklyHours > settings.getMaxWeeklyHours()) {
            issues.add(String.format(
                "Превышена недельная нагрузка: %.1f часов (максимум: %.1f)",
                weeklyHours, settings.getMaxWeeklyHours()
            ));
        }

        // Проверяем конфликты в расписании
        dailyEntries.forEach((date, entries) -> {
            for (int i = 0; i < entries.size(); i++) {
                for (int j = i + 1; j < entries.size(); j++) {
                    TimeEntry entry1 = entries.get(i);
                    TimeEntry entry2 = entries.get(j);
                    
                    if (entry1.getEndTime() != null && entry2.getEndTime() != null) {
                        if (!(entry1.getEndTime().isBefore(entry2.getStartTime()) || 
                            entry1.getStartTime().isAfter(entry2.getEndTime()))) {
                            issues.add(String.format(
                                "Конфликт в расписании %s: %s - %s и %s - %s",
                                date,
                                entry1.getStartTime().toLocalTime(),
                                entry1.getEndTime().toLocalTime(),
                                entry2.getStartTime().toLocalTime(),
                                entry2.getEndTime().toLocalTime()
                            ));
                        }
                    }
                }
            }
        });

        // Проверяем приближающиеся дедлайны
        LocalDate warningDate = now.plusDays(settings.getDeadlineWarningDays());
        projects.stream()
                .filter(p -> p.getStatus() != ProjectStatus.COMPLETED)
                .filter(p -> p.getDeadline() != null)
                .filter(p -> !p.getDeadline().isBefore(now))
                .filter(p -> !p.getDeadline().isAfter(warningDate))
                .forEach(p -> issues.add(String.format(
                    "Приближается дедлайн проекта '%s': %s",
                    p.getName(), p.getDeadline()
                )));

        return issues;
    }
} 
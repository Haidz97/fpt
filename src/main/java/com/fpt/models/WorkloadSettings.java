package com.fpt.models;

/**
 * Модель для хранения настроек планирования нагрузки.
 * Содержит параметры ограничений рабочего времени и настройки уведомлений.
 */
public class WorkloadSettings {
    /** Уникальный идентификатор настроек в базе данных */
    private Long id;
    
    /** Максимальное количество рабочих часов в день (от 1 до 24) */
    private double maxDailyHours;
    
    /** Максимальное количество рабочих часов в неделю (от 1 до 168) */
    private double maxWeeklyHours;
    
    /** Желаемое количество одновременно активных проектов */
    private int desiredProjectCount;
    
    /** Флаг включения уведомлений о приближающихся дедлайнах */
    private boolean enableDeadlineNotifications;
    
    /** Флаг включения уведомлений о превышении рабочего времени */
    private boolean enableWorkloadNotifications;
    
    /** Флаг включения уведомлений о конфликтах в расписании */
    private boolean enableScheduleConflictNotifications;
    
    /** За сколько дней начинать предупреждать о приближающемся дедлайне */
    private int deadlineWarningDays;

    /**
     * Конструктор по умолчанию.
     * Инициализирует настройки значениями по умолчанию:
     * - 8 часов в день
     * - 40 часов в неделю
     * - 3 проекта одновременно
     * - Все уведомления включены
     * - Предупреждение о дедлайне за 7 дней
     */
    public WorkloadSettings() {
        this.maxDailyHours = 8.0;
        this.maxWeeklyHours = 40.0;
        this.desiredProjectCount = 3;
        this.enableDeadlineNotifications = true;
        this.enableWorkloadNotifications = true;
        this.enableScheduleConflictNotifications = true;
        this.deadlineWarningDays = 7;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getMaxDailyHours() {
        return maxDailyHours;
    }

    public void setMaxDailyHours(double maxDailyHours) {
        this.maxDailyHours = maxDailyHours;
    }

    public double getMaxWeeklyHours() {
        return maxWeeklyHours;
    }

    public void setMaxWeeklyHours(double maxWeeklyHours) {
        this.maxWeeklyHours = maxWeeklyHours;
    }

    public int getDesiredProjectCount() {
        return desiredProjectCount;
    }

    public void setDesiredProjectCount(int desiredProjectCount) {
        this.desiredProjectCount = desiredProjectCount;
    }

    public boolean isEnableDeadlineNotifications() {
        return enableDeadlineNotifications;
    }

    public void setEnableDeadlineNotifications(boolean enableDeadlineNotifications) {
        this.enableDeadlineNotifications = enableDeadlineNotifications;
    }

    public boolean isEnableWorkloadNotifications() {
        return enableWorkloadNotifications;
    }

    public void setEnableWorkloadNotifications(boolean enableWorkloadNotifications) {
        this.enableWorkloadNotifications = enableWorkloadNotifications;
    }

    public boolean isEnableScheduleConflictNotifications() {
        return enableScheduleConflictNotifications;
    }

    public void setEnableScheduleConflictNotifications(boolean enableScheduleConflictNotifications) {
        this.enableScheduleConflictNotifications = enableScheduleConflictNotifications;
    }

    public int getDeadlineWarningDays() {
        return deadlineWarningDays;
    }

    public void setDeadlineWarningDays(int deadlineWarningDays) {
        this.deadlineWarningDays = deadlineWarningDays;
    }
} 
package com.fpt.models;

import java.time.LocalDateTime;

public class Project {
    private Long id;
    private String name;
    private String description;
    private String client;
    private ProjectStatus status;
    private LocalDateTime startDate;
    private LocalDateTime deadline;
    private double budget;
    private double hourlyRate;
    private double timeSpent; // в часах
    private double actualIncome;
    private ProjectSource source;

    public enum ProjectStatus {
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }

    public enum ProjectSource {
        UPWORK,
        OTHER
    }

    // Конструктор
    public Project() {
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public double getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(double timeSpent) {
        this.timeSpent = timeSpent;
    }

    public double getActualIncome() {
        return actualIncome;
    }

    public void setActualIncome(double actualIncome) {
        this.actualIncome = actualIncome;
    }

    public ProjectSource getSource() {
        return source;
    }

    public void setSource(ProjectSource source) {
        this.source = source;
    }

    // Методы для расчета показателей
    public double calculateHourlyEarning() {
        if (timeSpent <= 0) {
            return 0;
        }
        return actualIncome / timeSpent;
    }

    public double calculateProjectProgress() {
        if (deadline == null || startDate == null) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        long totalDuration = deadline.toEpochSecond(java.time.ZoneOffset.UTC) -
                startDate.toEpochSecond(java.time.ZoneOffset.UTC);
        long elapsedDuration = now.toEpochSecond(java.time.ZoneOffset.UTC) -
                startDate.toEpochSecond(java.time.ZoneOffset.UTC);

        if (totalDuration <= 0) {
            return 0;
        }

        return Math.min(100.0, (elapsedDuration * 100.0) / totalDuration);
    }

    public boolean isOverdue() {
        if (deadline == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(deadline);
    }
}
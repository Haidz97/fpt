package com.fpt.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Project {
    private Long id;
    private String name;
    private String description;
    private String client;
    private ProjectStatus status;
    private LocalDateTime startDate;
    private LocalDateTime deadline;
    private Double budget;
    private Double hourlyRate;
    private Long timeSpent; // в минутах
    private Double actualIncome;
    private ProjectSource source;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> categories;

    public Project() {
        this.categories = new ArrayList<>();
        this.status = ProjectStatus.NEW;
        this.source = ProjectSource.OTHER;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public Double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(Double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public Long getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(Long timeSpent) {
        this.timeSpent = timeSpent;
    }

    public Double getActualIncome() {
        return actualIncome;
    }

    public void setActualIncome(Double actualIncome) {
        this.actualIncome = actualIncome;
    }

    public ProjectSource getSource() {
        return source;
    }

    public void setSource(ProjectSource source) {
        this.source = source;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    // Вспомогательные методы
    public double getHoursSpent() {
        return timeSpent != null ? timeSpent / 60.0 : 0.0;
    }

    public boolean isOverdue() {
        return deadline != null && LocalDateTime.now().isAfter(deadline);
    }

    public double getProjectProgress() {
        if (budget == null || actualIncome == null) return 0.0;
        return (actualIncome / budget) * 100.0;
    }

    public double getEffectiveHourlyRate() {
        if (timeSpent == null || timeSpent == 0 || actualIncome == null) return 0.0;
        return (actualIncome / (timeSpent / 60.0));
    }
} 
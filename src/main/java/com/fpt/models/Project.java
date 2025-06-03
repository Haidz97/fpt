package com.fpt.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fpt.models.ProjectStatus;

public class Project {
    private Long id;
    private String name;
    private String description;
    private String client;
    private ProjectStatus status;
    private LocalDate startDate;
    private LocalDate deadline;
    private double budget;
    private double hourlyRate;
    private List<TimeEntry> timeEntries;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Project() {
        this.timeEntries = new ArrayList<>();
        this.status = ProjectStatus.NEW;
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
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

    public List<TimeEntry> getTimeEntries() {
        return timeEntries;
    }

    public void setTimeEntries(List<TimeEntry> timeEntries) {
        this.timeEntries = timeEntries;
    }

    public void addTimeEntry(TimeEntry timeEntry) {
        this.timeEntries.add(timeEntry);
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

    // Вспомогательные методы
    public double getTotalTimeSpent() {
        return timeEntries.stream()
                .filter(entry -> entry.getEndTime() != null)
                .mapToLong(TimeEntry::getDurationInMinutes)
                .sum() / 60.0;
    }

    public double getTotalEarned() {
        return getTotalTimeSpent() * hourlyRate;
    }

    public double getRemainingBudget() {
        return budget - getTotalEarned();
    }

    public boolean isOverBudget() {
        return getTotalEarned() > budget;
    }

    public boolean isDeadlineMissed() {
        return deadline != null && LocalDate.now().isAfter(deadline);
    }
} 
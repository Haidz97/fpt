package com.fpt.models;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TimeEntry {
    private Long id;
    private Project project;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;

    public TimeEntry() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getDurationInHours() {
        if (startTime == null || endTime == null) {
            return 0.0;
        }
        long minutes = ChronoUnit.MINUTES.between(startTime, endTime);
        return minutes / 60.0;
    }
} 
package com.fpt.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class WorkloadPlan {
    private Long id;
    private LocalDate date;
    private double plannedHours;
    private double maxHoursPerDay;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public WorkloadPlan() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.maxHoursPerDay = 8.0; // Стандартный 8-часовой рабочий день
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getPlannedHours() {
        return plannedHours;
    }

    public void setPlannedHours(double plannedHours) {
        this.plannedHours = plannedHours;
    }

    public double getMaxHoursPerDay() {
        return maxHoursPerDay;
    }

    public void setMaxHoursPerDay(double maxHoursPerDay) {
        this.maxHoursPerDay = maxHoursPerDay;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public boolean isOverloaded() {
        return plannedHours > maxHoursPerDay;
    }

    public double getAvailableHours() {
        return Math.max(0, maxHoursPerDay - plannedHours);
    }

    public double getOverloadHours() {
        return Math.max(0, plannedHours - maxHoursPerDay);
    }
} 
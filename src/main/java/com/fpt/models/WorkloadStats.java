package com.fpt.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WorkloadStats {
    private LocalDate date;
    private double hoursWorked;
    private double remainingHours;
    private List<TimeEntry> timeEntries;
    private static final double MAX_DAILY_HOURS = 8.0;

    public WorkloadStats(LocalDate date, List<TimeEntry> timeEntries) {
        this.date = date;
        this.timeEntries = timeEntries;
        calculateStats();
    }

    private void calculateStats() {
        hoursWorked = timeEntries.stream()
                .filter(entry -> {
                    LocalDateTime start = entry.getStartTime();
                    LocalDateTime end = entry.getEndTime();
                    return start != null &&
                            start.toLocalDate().equals(date) &&
                            (end == null || end.toLocalDate().equals(date));
                })
                .mapToDouble(entry -> {
                    LocalDateTime end = entry.getEndTime() != null ?
                            entry.getEndTime() :
                            LocalDateTime.now();
                    long minutes = ChronoUnit.MINUTES.between(entry.getStartTime(), end);
                    return minutes / 60.0;
                })
                .sum();

        remainingHours = Math.max(0, MAX_DAILY_HOURS - hoursWorked);
    }

    public LocalDate getDate() {
        return date;
    }

    public double getHoursWorked() {
        return hoursWorked;
    }

    public double getRemainingHours() {
        return remainingHours;
    }

    public boolean isOverworked() {
        return hoursWorked > MAX_DAILY_HOURS;
    }

    public String getWorkloadStatus() {
        if (isOverworked()) {
            return "Переработка: +" + String.format("%.1f", hoursWorked - MAX_DAILY_HOURS) + " ч";
        } else if (hoursWorked == MAX_DAILY_HOURS) {
            return "Дневная норма выполнена";
        } else {
            return "Осталось: " + String.format("%.1f", remainingHours) + " ч";
        }
    }

    public String getWorkloadWarning() {
        if (isOverworked()) {
            return "Внимание! Вы превысили рекомендуемую дневную нагрузку в " + MAX_DAILY_HOURS + " часов";
        }
        return null;
    }

    public static Map<LocalDate, WorkloadStats> createWeeklyStats(List<TimeEntry> allEntries) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6); // последние 7 дней

        return allEntries.stream()
                .filter(entry -> {
                    LocalDateTime start = entry.getStartTime();
                    return start != null &&
                            !start.toLocalDate().isBefore(weekStart) &&
                            !start.toLocalDate().isAfter(today);
                })
                .collect(Collectors.groupingBy(
                        entry -> entry.getStartTime().toLocalDate(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                entries -> new WorkloadStats(entries.get(0).getStartTime().toLocalDate(), entries)
                        )
                ));
    }
} 
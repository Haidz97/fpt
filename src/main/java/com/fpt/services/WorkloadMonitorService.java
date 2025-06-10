package com.fpt.services;

import com.fpt.models.TimeEntry;
import com.fpt.models.WorkloadAlert;
import com.fpt.utils.DatabaseManager;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.time.temporal.ChronoUnit;

public class WorkloadMonitorService {
    private final TimeEntryService timeEntryService;

    public WorkloadMonitorService(DatabaseManager dbManager) {
        if (dbManager == null) {
            throw new IllegalArgumentException("DatabaseManager cannot be null");
        }
        this.timeEntryService = new TimeEntryService(dbManager);
    }

    public WorkloadAlert getCurrentDayAlert() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        double hoursWorked = calculateHoursWorked(startOfDay, now);
        return new WorkloadAlert(now, hoursWorked);
    }

    public List<WorkloadAlert> getWeeklyAlerts() {
        List<WorkloadAlert> alerts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDate startOfWeek = now.toLocalDate().minusDays(6); // последние 7 дней

        for (LocalDate date = startOfWeek; !date.isAfter(now.toLocalDate()); date = date.plusDays(1)) {
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.equals(now.toLocalDate()) ? now : date.plusDays(1).atStartOfDay();
            double hoursWorked = calculateHoursWorked(dayStart, dayEnd);
            alerts.add(new WorkloadAlert(dayStart, hoursWorked));
        }

        return alerts;
    }

    public Map<LocalDate, Double> getWeeklyWorkload() {
        Map<LocalDate, Double> workload = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDate startOfWeek = now.toLocalDate().minusDays(6);

        List<TimeEntry> entries = timeEntryService.getAllTimeEntries();
        if (entries == null) {
            return workload;
        }
        
        for (TimeEntry entry : entries) {
            if (entry != null && entry.getStartTime() != null) {
                LocalDate entryDate = entry.getStartTime().toLocalDate();
                if (!entryDate.isBefore(startOfWeek) && !entryDate.isAfter(now.toLocalDate())) {
                    double hours = calculateEntryHours(entry);
                    workload.merge(entryDate, hours, Double::sum);
                }
            }
        }

        return workload;
    }

    private double calculateHoursWorked(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || end.isBefore(start)) {
            return 0.0;
        }

        List<TimeEntry> entries = timeEntryService.getAllTimeEntries();
        if (entries == null) {
            return 0.0;
        }

        double totalHours = 0.0;

        for (TimeEntry entry : entries) {
            if (entry != null && entry.getStartTime() != null && 
                entry.getStartTime().isAfter(start) && 
                (entry.getEndTime() == null || entry.getEndTime().isBefore(end))) {
                
                totalHours += calculateEntryHours(entry);
            }
        }

        return totalHours;
    }

    private double calculateEntryHours(TimeEntry entry) {
        if (entry == null || entry.getStartTime() == null) {
            return 0.0;
        }

        LocalDateTime entryEnd = entry.getEndTime() != null ? entry.getEndTime() : LocalDateTime.now();
        if (entryEnd.isBefore(entry.getStartTime())) {
            return 0.0;
        }

        long minutes = ChronoUnit.MINUTES.between(entry.getStartTime(), entryEnd);
        return minutes / 60.0;
    }

    public String getWorkloadRecommendation(WorkloadAlert alert) {
        if (alert == null) {
            return "Нет данных о нагрузке";
        }

        if (alert.getLevel() == WorkloadAlert.AlertLevel.CRITICAL) {
            return "Рекомендуется завершить работу. Вы превысили дневную норму на " + 
                String.format("%.1f", alert.getHoursWorked() - WorkloadAlert.MAX_DAILY_HOURS) + " часов.";
        } else if (alert.getLevel() == WorkloadAlert.AlertLevel.WARNING) {
            return "Осталось " + String.format("%.1f", alert.getRemainingHours()) + 
                " часов до превышения дневной нормы. Планируйте оставшееся время.";
        }
        return "Нормальный режим работы. Доступно еще " + 
            String.format("%.1f", alert.getRemainingHours()) + " часов на сегодня.";
    }
} 
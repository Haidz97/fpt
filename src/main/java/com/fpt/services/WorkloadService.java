package com.fpt.services;

import com.fpt.models.Project;
import com.fpt.models.TimeEntry;
import com.fpt.utils.DatabaseManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkloadService {
    private final ProjectService projectService;
    private final TimeEntryService timeEntryService;

    public WorkloadService(DatabaseManager dbManager) {
        this.projectService = new ProjectService(dbManager);
        this.timeEntryService = new TimeEntryService(dbManager);
    }

    public Map<Project, Double> getWeeklyWorkload() {
        Map<Project, Double> workload = new HashMap<>();
        LocalDateTime weekStart = LocalDateTime.now().with(java.time.DayOfWeek.MONDAY).withHour(0).withMinute(0);
        LocalDateTime weekEnd = weekStart.plusDays(7);

        List<Project> projects = projectService.getAllProjects();
        for (Project project : projects) {
            double hours = 0;
            List<TimeEntry> entries = timeEntryService.getTimeEntriesForProject(project.getId());
            
            for (TimeEntry entry : entries) {
                if (entry.getStartTime().isAfter(weekStart) && 
                    entry.getStartTime().isBefore(weekEnd) &&
                    entry.getEndTime() != null) {
                    hours += entry.getDurationInHours();
                }
            }
            
            if (hours > 0) {
                workload.put(project, hours);
            }
        }

        return workload;
    }

    public Map<LocalDate, Double> getDailyWorkload() {
        Map<LocalDate, Double> dailyHours = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(java.time.DayOfWeek.MONDAY);

        List<Project> projects = projectService.getAllProjects();
        for (Project project : projects) {
            List<TimeEntry> entries = timeEntryService.getTimeEntriesForProject(project.getId());
            
            for (TimeEntry entry : entries) {
                if (entry.getEndTime() != null) {
                    LocalDate entryDate = entry.getStartTime().toLocalDate();
                    if (!entryDate.isBefore(weekStart)) {
                        double hours = entry.getDurationInHours();
                        dailyHours.merge(entryDate, hours, Double::sum);
                    }
                }
            }
        }

        return dailyHours;
    }
} 
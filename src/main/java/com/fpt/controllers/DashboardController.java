package com.fpt.controllers;

import com.fpt.models.Project;
import com.fpt.models.ProjectStatus;
import com.fpt.models.TimeEntry;
import com.fpt.services.ProjectService;
import com.fpt.services.TimeEntryService;
import com.fpt.utils.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardController {
    @FXML private Label activeProjectsCount;
    @FXML private Label monthlyIncome;
    @FXML private Label averageHourlyRate;
    @FXML private Label weeklyHours;
    @FXML private LineChart<Number, Number> incomeChart;
    @FXML private PieChart timeDistributionChart;
    @FXML private ListView<String> recentProjectsList;
    @FXML private ListView<String> upcomingDeadlinesList;

    private ProjectService projectService;
    private TimeEntryService timeEntryService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @FXML
    public void initialize() {
        projectService = new ProjectService(DatabaseManager.getInstance());
        timeEntryService = new TimeEntryService(DatabaseManager.getInstance());
        updateDashboard();
    }

    private void updateDashboard() {
        List<Project> allProjects = projectService.getAllProjects();
        
        updateProjectStats(allProjects);
        updateCharts(allProjects);
        updateLists(allProjects);
    }

    private void updateProjectStats(List<Project> projects) {
        // Активные проекты
        long activeCount = projects.stream()
                .filter(p -> p.getStatus() == ProjectStatus.IN_PROGRESS)
                .count();
        activeProjectsCount.setText(String.valueOf(activeCount));

        // Подсчет доходов и часов
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0);
        LocalDateTime weekStart = now.with(java.time.DayOfWeek.MONDAY).withHour(0).withMinute(0);

        double monthlyIncomeValue = 0.0;
        double totalWeeklyHours = 0.0;
        double totalHourlyRate = 0.0;
        int projectsWithRate = 0;

        for (Project project : projects) {
            if (project.getStatus() == ProjectStatus.IN_PROGRESS) {
                List<TimeEntry> entries = timeEntryService.getTimeEntriesForProject(project.getId());
                
                for (TimeEntry entry : entries) {
                    if (entry.getEndTime() != null) {
                        double hours = entry.getDurationInHours();
                        
                        if (entry.getStartTime().isAfter(monthStart)) {
                            monthlyIncomeValue += hours * project.getHourlyRate();
                        }
                        
                        if (entry.getStartTime().isAfter(weekStart)) {
                            totalWeeklyHours += hours;
                        }
                    }
                }

                if (project.getHourlyRate() > 0) {
                    totalHourlyRate += project.getHourlyRate();
                    projectsWithRate++;
                }
            }
        }

        monthlyIncome.setText(String.format("%,.2f ₽", monthlyIncomeValue));
        weeklyHours.setText(String.format("%.1f", totalWeeklyHours));
        
        double avgHourlyRate = projectsWithRate > 0 ? totalHourlyRate / projectsWithRate : 0;
        averageHourlyRate.setText(String.format("%,.2f ₽/час", avgHourlyRate));
    }

    private void updateCharts(List<Project> projects) {
        // График доходов
        XYChart.Series<Number, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Доход");
        
        // Здесь можно добавить реальные данные по месяцам
        // Пока добавим тестовые данные
        for (int i = 1; i <= 12; i++) {
            incomeSeries.getData().add(new XYChart.Data<>(i, Math.random() * 100000));
        }
        
        incomeChart.getData().clear();
        incomeChart.getData().add(incomeSeries);

        // График распределения времени
        timeDistributionChart.getData().clear();
        projects.stream()
                .filter(p -> p.getStatus() == ProjectStatus.IN_PROGRESS)
                .forEach(project -> {
                    double totalHours = timeEntryService.getTimeEntriesForProject(project.getId())
                            .stream()
                            .filter(entry -> entry.getEndTime() != null)
                            .mapToDouble(TimeEntry::getDurationInHours)
                            .sum();
                    if (totalHours > 0) {
                        timeDistributionChart.getData().add(
                            new PieChart.Data(project.getName(), totalHours)
                        );
                    }
                });
    }

    private void updateLists(List<Project> projects) {
        // Последние проекты
        recentProjectsList.getItems().clear();
        projects.stream()
                .sorted(Comparator.comparing(Project::getStartDate).reversed())
                .limit(5)
                .forEach(project -> recentProjectsList.getItems().add(
                    String.format("%s (%s)", project.getName(), project.getStatus())
                ));

        // Ближайшие дедлайны
        upcomingDeadlinesList.getItems().clear();
        projects.stream()
                .filter(p -> p.getDeadline() != null && p.getStatus() != ProjectStatus.COMPLETED)
                .sorted(Comparator.comparing(Project::getDeadline))
                .limit(5)
                .forEach(project -> upcomingDeadlinesList.getItems().add(
                    String.format("%s (%s)", project.getName(), project.getDeadline().format(DATE_FORMATTER))
                ));
    }
} 
package com.fpt.controllers;

import com.fpt.models.Project;
import com.fpt.models.TimeEntry;
import com.fpt.services.ProjectService;
import com.fpt.services.TimeEntryService;
import com.fpt.services.WorkloadService;
import com.fpt.utils.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class WorkloadMonitorController implements Initializable {
    @FXML 
    private PieChart workloadPieChart;
    
    private TimeEntryService timeEntryService;
    private ProjectService projectService;
    private WorkloadService workloadService;
    private static final double MAX_DAILY_HOURS = 8.0;

    public WorkloadMonitorController() {
        System.out.println("Creating WorkloadMonitorController");
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            if (dbManager == null) {
                throw new IllegalStateException("DatabaseManager is null");
            }
            
            this.timeEntryService = new TimeEntryService(dbManager);
            this.projectService = new ProjectService(dbManager);
            this.workloadService = new WorkloadService(dbManager);
            
            System.out.println("Services initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing services: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Initializing WorkloadMonitorController");
        try {
            loadTestData();
            System.out.println("WorkloadMonitorController initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing WorkloadMonitorController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadTestData() {
        // Создаем тестовые данные для диаграммы
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Веб-разработка", 25),
            new PieChart.Data("Мобильное приложение", 30),
            new PieChart.Data("Дизайн", 15),
            new PieChart.Data("Тестирование", 20),
            new PieChart.Data("Документация", 10)
        );
        
        workloadPieChart.setData(pieChartData);
        workloadPieChart.setTitle("Распределение времени по типам работ");
        
        // Добавляем всплывающие подсказки с процентами
        pieChartData.forEach(data -> {
            double percentage = (data.getPieValue() / pieChartData.stream()
                .mapToDouble(PieChart.Data::getPieValue)
                .sum()) * 100;
            
            String tooltipText = String.format("%s: %.1f часов (%.1f%%)", 
                data.getName(), data.getPieValue(), percentage);
            
            Tooltip tooltip = new Tooltip(tooltipText);
            Tooltip.install(data.getNode(), tooltip);
        });
        
        // Добавляем анимацию при запуске
        workloadPieChart.setAnimated(true);
    }
} 
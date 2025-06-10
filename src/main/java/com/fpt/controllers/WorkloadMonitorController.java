package com.fpt.controllers;

import com.fpt.models.WorkloadAlert;
import com.fpt.services.WorkloadMonitorService;
import com.fpt.utils.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.application.Platform;
import javafx.scene.paint.Color;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WorkloadMonitorController {
    @FXML private Label currentWorkloadLabel;
    @FXML private ProgressBar workloadProgressBar;
    @FXML private Label recommendationLabel;
    @FXML private BarChart<String, Number> weeklyChart;
    @FXML private ListView<String> alertsList;

    private WorkloadMonitorService monitorService;
    private Timer updateTimer;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM");

    @FXML
    public void initialize() {
        monitorService = new WorkloadMonitorService(DatabaseManager.getInstance());
        
        // Настраиваем автоматическое обновление каждую минуту
        updateTimer = new Timer(true);
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> updateWorkloadInfo());
            }
        }, 0, 60000); // Обновление каждую минуту

        updateWorkloadInfo();
    }

    private void updateWorkloadInfo() {
        // Обновляем текущую нагрузку
        WorkloadAlert currentAlert = monitorService.getCurrentDayAlert();
        updateCurrentWorkload(currentAlert);

        // Обновляем график за неделю
        updateWeeklyChart();

        // Обновляем список предупреждений
        updateAlertsList();
    }

    private void updateCurrentWorkload(WorkloadAlert alert) {
        currentWorkloadLabel.setText(alert.getMessage());
        currentWorkloadLabel.setTextFill(alert.getColor());

        double progress = Math.min(1.0, alert.getHoursWorked() / WorkloadAlert.MAX_DAILY_HOURS);
        workloadProgressBar.setProgress(progress);
        
        // Устанавливаем цвет прогресс-бара в зависимости от уровня нагрузки
        String style = String.format("-fx-accent: %s;", 
            alert.getColor() == Color.GREEN ? "green" :
            alert.getColor() == Color.ORANGE ? "orange" : "red");
        workloadProgressBar.setStyle(style);

        recommendationLabel.setText(monitorService.getWorkloadRecommendation(alert));
    }

    private void updateWeeklyChart() {
        weeklyChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        List<WorkloadAlert> weeklyAlerts = monitorService.getWeeklyAlerts();
        for (WorkloadAlert alert : weeklyAlerts) {
            String day = alert.getDate().format(DATE_FORMATTER);
            XYChart.Data<String, Number> data = new XYChart.Data<>(day, alert.getHoursWorked());
            series.getData().add(data);
            
            // Устанавливаем цвет столбца в зависимости от уровня нагрузки
            Platform.runLater(() -> {
                if (data.getNode() != null) {
                    data.getNode().setStyle(String.format("-fx-bar-fill: %s;",
                        alert.getColor() == Color.GREEN ? "green" :
                        alert.getColor() == Color.ORANGE ? "orange" : "red"));
                }
            });
        }

        weeklyChart.getData().add(series);
    }

    private void updateAlertsList() {
        alertsList.getItems().clear();
        List<WorkloadAlert> alerts = monitorService.getWeeklyAlerts();
        
        for (WorkloadAlert alert : alerts) {
            if (alert.getLevel() != WorkloadAlert.AlertLevel.NORMAL) {
                String alertText = String.format("%s: %s", 
                    alert.getDate().format(DATE_FORMATTER), 
                    alert.getMessage());
                alertsList.getItems().add(alertText);
            }
        }
    }

    public void shutdown() {
        if (updateTimer != null) {
            updateTimer.cancel();
        }
    }
} 
package com.fpt.controllers;

import com.fpt.models.TimeEntry;
import com.fpt.models.WorkloadStats;
import com.fpt.services.TimeEntryService;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class WorkloadController {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM");

    @FXML private Label hoursWorkedLabel;
    @FXML private Label remainingHoursLabel;
    @FXML private Label workloadStatusLabel;
    @FXML private Label warningLabel;
    @FXML private BarChart<String, Number> weeklyChart;
    @FXML private TextFlow recommendationsFlow;

    private TimeEntryService timeEntryService;
    private WorkloadStats todayStats;
    private Map<LocalDate, WorkloadStats> weeklyStats;

    public void initialize() {
        setupChartStyle();
    }

    public void setTimeEntryService(TimeEntryService timeEntryService) {
        this.timeEntryService = timeEntryService;
    }

    private void setupChartStyle() {
        weeklyChart.setAnimated(false);
        weeklyChart.setBarGap(2);
        weeklyChart.setCategoryGap(20);
    }

    public void updateWorkloadStats() {
        List<TimeEntry> allEntries = timeEntryService.getAllTimeEntries();
        weeklyStats = WorkloadStats.createWeeklyStats(allEntries);

        // Статистика за сегодня
        todayStats = weeklyStats.get(LocalDate.now());
        if (todayStats != null) {
            updateTodayStats();
        }

        // График за неделю
        updateWeeklyChart();

        // Рекомендации
        updateRecommendations();
    }

    private void updateTodayStats() {
        hoursWorkedLabel.setText(String.format("%.1f ч", todayStats.getHoursWorked()));
        remainingHoursLabel.setText(String.format("%.1f ч", todayStats.getRemainingHours()));
        workloadStatusLabel.setText(todayStats.getWorkloadStatus());

        String warning = todayStats.getWorkloadWarning();
        if (warning != null) {
            warningLabel.setText(warning);
            warningLabel.setVisible(true);
            warningLabel.setManaged(true);
        } else {
            warningLabel.setVisible(false);
            warningLabel.setManaged(false);
        }
    }

    private void updateWeeklyChart() {
        weeklyChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            WorkloadStats stats = weeklyStats.getOrDefault(
                    date,
                    new WorkloadStats(date, List.of())
            );

            XYChart.Data<String, Number> data = new XYChart.Data<>(
                    date.format(DATE_FORMATTER),
                    stats.getHoursWorked()
            );

            // Стилизация столбцов в зависимости от нагрузки
            data.nodeProperty().addListener((obs, old, node) -> {
                if (node != null) {
                    if (stats.isOverworked()) {
                        node.setStyle("-fx-bar-fill: #ff6b6b;"); // красный для переработки
                    } else if (stats.getHoursWorked() == 8.0) {
                        node.setStyle("-fx-bar-fill: #51cf66;"); // зеленый для нормы
                    } else {
                        node.setStyle("-fx-bar-fill: #339af0;"); // синий для недоработки
                    }
                }
            });

            series.getData().add(data);
        }

        weeklyChart.getData().add(series);
    }

    private void updateRecommendations() {
        recommendationsFlow.getChildren().clear();

        if (todayStats == null || !todayStats.isOverworked()) {
            double weeklyHours = weeklyStats.values().stream()
                    .mapToDouble(WorkloadStats::getHoursWorked)
                    .sum();

            if (weeklyHours > 40) {
                addRecommendation("⚠️ На этой неделе вы работаете больше 40 часов. " +
                        "Рекомендуется снизить нагрузку в следующие дни.", Color.ORANGERED);
            }

            // Проверяем последние 3 дня на переработки
            long overworkedDays = weeklyStats.values().stream()
                    .filter(WorkloadStats::isOverworked)
                    .count();

            if (overworkedDays >= 2) {
                addRecommendation("\n⚡ У вас было несколько дней с переработками. " +
                        "Постарайтесь лучше планировать рабочее время.", Color.ORANGERED);
            }

            if (todayStats != null && todayStats.getHoursWorked() < 8.0) {
                addRecommendation("\n✨ Сегодня у вас еще есть " +
                        String.format("%.1f", todayStats.getRemainingHours()) +
                        " часов до нормы. Распределите оставшееся время эффективно.", Color.GREEN);
            }
        } else {
            addRecommendation("🛑 Рекомендуется завершить работу на сегодня. " +
                            "Продолжительная работа может привести к снижению продуктивности и усталости.",
                    Color.RED);
        }

        addRecommendation("\n\n💡 Общие рекомендации:\n" +
                        "• Делайте перерыв каждые 1-2 часа\n" +
                        "• Не забывайте про физическую активность\n" +
                        "• Следите за правильной осанкой\n" +
                        "• Регулярно проветривайте помещение",
                Color.BLACK);
    }

    private void addRecommendation(String message, Color color) {
        Text text = new Text(message);
        text.setFill(color);
        recommendationsFlow.getChildren().add(text);
    }
} 
package com.fpt.controllers;

import com.fpt.models.Project;
import com.fpt.models.TimeEntry;
import com.fpt.services.ProjectService;
import com.fpt.services.TimeEntryService;
import com.fpt.services.PdfExportService;
import com.fpt.utils.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleDoubleProperty;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ProjectViewController {
    @FXML private Label projectNameLabel;
    @FXML private Label clientLabel;
    @FXML private Label statusLabel;
    @FXML private Label startDateLabel;
    @FXML private Label deadlineLabel;
    @FXML private Label budgetLabel;
    @FXML private Label hourlyRateLabel;
    @FXML private TextArea descriptionArea;
    @FXML private TableView<TimeEntry> timeEntriesTable;
    @FXML private TableColumn<TimeEntry, String> dateColumn;
    @FXML private TableColumn<TimeEntry, String> startTimeColumn;
    @FXML private TableColumn<TimeEntry, String> endTimeColumn;
    @FXML private TableColumn<TimeEntry, Number> durationColumn;
    @FXML private TableColumn<TimeEntry, String> descriptionColumn;
    @FXML private Label totalTimeLabel;
    @FXML private Label totalEarnedLabel;
    @FXML private Label remainingBudgetLabel;

    private Project project;
    private ProjectService projectService;
    private TimeEntryService timeEntryService;
    private PdfExportService pdfExportService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        projectService = new ProjectService(DatabaseManager.getInstance());
        timeEntryService = new TimeEntryService(DatabaseManager.getInstance());
        pdfExportService = new PdfExportService();
        setupTimeEntriesTable();
    }

    private void setupTimeEntriesTable() {
        dateColumn.setCellValueFactory(cellData -> {
            TimeEntry entry = cellData.getValue();
            String date = entry.getStartTime().format(DATE_FORMATTER);
            return javafx.beans.binding.Bindings.createStringBinding(() -> date);
        });

        startTimeColumn.setCellValueFactory(cellData -> {
            TimeEntry entry = cellData.getValue();
            String time = entry.getStartTime().format(TIME_FORMATTER);
            return javafx.beans.binding.Bindings.createStringBinding(() -> time);
        });

        endTimeColumn.setCellValueFactory(cellData -> {
            TimeEntry entry = cellData.getValue();
            String time = entry.getEndTime() != null ? entry.getEndTime().format(TIME_FORMATTER) : "В процессе";
            return javafx.beans.binding.Bindings.createStringBinding(() -> time);
        });

        durationColumn.setCellValueFactory(cellData -> {
            TimeEntry entry = cellData.getValue();
            return new SimpleDoubleProperty(entry.getDurationInHours());
        });

        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
    }

    public void setProject(Project project) {
        this.project = project;
        updateProjectView();
    }

    private void updateProjectView() {
        if (project != null) {
            projectNameLabel.setText(project.getName());
            clientLabel.setText(project.getClient());
            statusLabel.setText(project.getStatus().toString());
            startDateLabel.setText(project.getStartDate().format(DATE_FORMATTER));
            deadlineLabel.setText(project.getDeadline() != null ? project.getDeadline().format(DATE_FORMATTER) : "Не задан");
            budgetLabel.setText(String.format("%,.2f ₽", project.getBudget()));
            hourlyRateLabel.setText(String.format("%,.2f ₽/час", project.getHourlyRate()));
            descriptionArea.setText(project.getDescription());

            List<TimeEntry> timeEntries = timeEntryService.getTimeEntriesForProject(project.getId());
            timeEntriesTable.getItems().setAll(timeEntries);

            double totalHours = timeEntries.stream()
                    .filter(entry -> entry.getEndTime() != null)
                    .mapToDouble(TimeEntry::getDurationInHours)
                    .sum();
            totalTimeLabel.setText(String.format("%.1f ч", totalHours));

            double totalEarned = totalHours * project.getHourlyRate();
            totalEarnedLabel.setText(String.format("%,.2f ₽", totalEarned));

            double remainingBudget = project.getBudget() - totalEarned;
            remainingBudgetLabel.setText(String.format("%,.2f ₽", remainingBudget));
        }
    }

    @FXML
    private void handleExportPdf() {
        try {
            pdfExportService.exportProject(project);
            showSuccess("Экспорт завершен", "Отчет сохранен успешно");
        } catch (Exception e) {
            showError("Ошибка экспорта", "Не удалось экспортировать отчет: " + e.getMessage());
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuccess(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 
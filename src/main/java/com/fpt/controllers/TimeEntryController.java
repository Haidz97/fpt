package com.fpt.controllers;

import com.fpt.models.TimeEntry;
import com.fpt.models.Project;
import com.fpt.models.WorkloadAlert;
import com.fpt.services.TimeEntryService;
import com.fpt.services.WorkloadMonitorService;
import com.fpt.utils.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDateTime;
import javafx.scene.paint.Color;
import java.util.Optional;

public class TimeEntryController {
    @FXML private TextArea descriptionField;
    @FXML private DatePicker datePicker;
    @FXML private TextField startTimeField;
    @FXML private TextField endTimeField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label workloadWarningLabel;

    private TimeEntryService timeEntryService;
    private WorkloadMonitorService workloadMonitorService;
    private Project project;
    private TimeEntry timeEntry;
    private Stage stage;
    private boolean isSaved = false;

    @FXML
    public void initialize() {
        timeEntryService = new TimeEntryService(DatabaseManager.getInstance());
        workloadMonitorService = new WorkloadMonitorService(DatabaseManager.getInstance());
        workloadWarningLabel.setVisible(false);
        saveButton.setOnAction(event -> handleSave());
        cancelButton.setOnAction(event -> handleCancel());
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setTimeEntry(TimeEntry timeEntry) {
        this.timeEntry = timeEntry;
        if (timeEntry == null) {
            this.timeEntry = new TimeEntry();
            this.timeEntry.setProject(project);
            this.timeEntry.setStartTime(LocalDateTime.now());
        } else {
            descriptionField.setText(timeEntry.getDescription());
            if (timeEntry.getStartTime() != null) {
                datePicker.setValue(timeEntry.getStartTime().toLocalDate());
                startTimeField.setText(timeEntry.getStartTime().toLocalTime().toString());
            }
            if (timeEntry.getEndTime() != null) {
                endTimeField.setText(timeEntry.getEndTime().toLocalTime().toString());
            }
        }
    }

    public TimeEntry getTimeEntry() {
        return isSaved ? timeEntry : null;
    }

    @FXML
    private void handleSave() {
        try {
            LocalDateTime startTime = LocalDateTime.of(
                datePicker.getValue(),
                LocalDateTime.parse(startTimeField.getText()).toLocalTime()
            );

            LocalDateTime endTime = null;
            if (!endTimeField.getText().isEmpty()) {
                endTime = LocalDateTime.of(
                    datePicker.getValue(),
                    LocalDateTime.parse(endTimeField.getText()).toLocalTime()
                );
            }

            // Проверяем нагрузку перед сохранением
            if (endTime != null) {
                WorkloadAlert alert = workloadMonitorService.getCurrentDayAlert();
                if (alert.getLevel() == WorkloadAlert.AlertLevel.WARNING || 
                    alert.getLevel() == WorkloadAlert.AlertLevel.CRITICAL) {
                    
                    String recommendation = workloadMonitorService.getWorkloadRecommendation(alert);
                    boolean proceed = showWorkloadWarningDialog(alert, recommendation);
                    if (!proceed) {
                        return;
                    }
                }
            }

            if (timeEntry == null) {
                timeEntry = new TimeEntry();
            }

            timeEntry.setProject(project);
            timeEntry.setStartTime(startTime);
            timeEntry.setEndTime(endTime);
            timeEntry.setDescription(descriptionField.getText());

            timeEntryService.saveTimeEntry(timeEntry);
            isSaved = true;
            closeWindow();

        } catch (Exception e) {
            showError("Ошибка", "Не удалось сохранить запись: " + e.getMessage());
        }
    }

    private boolean showWorkloadWarningDialog(WorkloadAlert alert, String recommendation) {
        Alert warningDialog = new Alert(Alert.AlertType.WARNING);
        warningDialog.setTitle("Предупреждение о нагрузке");
        warningDialog.setHeaderText(alert.getMessage());
        warningDialog.setContentText(recommendation + "\n\nПродолжить сохранение?");

        ButtonType continueButton = new ButtonType("Продолжить");
        ButtonType cancelButton = new ButtonType("Отменить", ButtonBar.ButtonData.CANCEL_CLOSE);
        warningDialog.getButtonTypes().setAll(continueButton, cancelButton);

        Optional<ButtonType> result = warningDialog.showAndWait();
        return result.isPresent() && result.get() == continueButton;
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateInput() {
        if (descriptionField.getText().isEmpty()) {
            showError("Validation Error", "Description is required");
            return false;
        }
        return true;
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeWindow() {
        stage.close();
    }

    @FXML
    private void updateWorkloadWarning() {
        if (!endTimeField.getText().isEmpty()) {
            WorkloadAlert alert = workloadMonitorService.getCurrentDayAlert();
            if (alert.getLevel() != WorkloadAlert.AlertLevel.NORMAL) {
                workloadWarningLabel.setText(alert.getMessage());
                workloadWarningLabel.setTextFill(alert.getColor());
                workloadWarningLabel.setVisible(true);
            } else {
                workloadWarningLabel.setVisible(false);
            }
        }
    }
}
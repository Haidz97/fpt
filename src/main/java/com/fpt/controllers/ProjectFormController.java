package com.fpt.controllers;

import com.fpt.models.Project;
import com.fpt.models.ProjectStatus;
import com.fpt.services.ProjectService;
import com.fpt.utils.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class ProjectFormController {
    @FXML private TextField nameField;
    @FXML private TextField clientField;
    @FXML private ComboBox<ProjectStatus> statusComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker deadlinePicker;
    @FXML private TextField budgetField;
    @FXML private TextField hourlyRateField;
    @FXML private TextArea descriptionArea;

    private Project project;
    private ProjectService projectService;

    @FXML
    public void initialize() {
        projectService = new ProjectService(DatabaseManager.getInstance());
        statusComboBox.getItems().addAll(ProjectStatus.values());

        // Добавьте числовую проверку для полей бюджета и почасовой ставки
        budgetField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                budgetField.setText(oldValue);
            }
        });

        hourlyRateField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                hourlyRateField.setText(oldValue);
            }
        });
    }

    public void setProject(Project project) {
        this.project = project;
        if (project != null) {
            nameField.setText(project.getName());
            clientField.setText(project.getClient());
            statusComboBox.setValue(project.getStatus());
            startDatePicker.setValue(project.getStartDate());
            deadlinePicker.setValue(project.getDeadline());
            budgetField.setText(String.valueOf(project.getBudget()));
            hourlyRateField.setText(String.valueOf(project.getHourlyRate()));
            descriptionArea.setText(project.getDescription());
        } else {
            this.project = new Project();
            statusComboBox.setValue(ProjectStatus.NEW);
            startDatePicker.setValue(LocalDate.now());
        }
    }

    @FXML
    public void handleSave() {
        if (validateInput()) {
            project.setName(nameField.getText());
            project.setClient(clientField.getText());
            project.setStatus(statusComboBox.getValue());
            project.setStartDate(startDatePicker.getValue());
            project.setDeadline(deadlinePicker.getValue());
            project.setBudget(Double.parseDouble(budgetField.getText()));
            project.setHourlyRate(Double.parseDouble(hourlyRateField.getText()));
            project.setDescription(descriptionArea.getText());

            projectService.saveProject(project);
            closeWindow();
        }
    }

    @FXML
    public void handleCancel() {
        closeWindow();
    }

    private boolean validateInput() {
        if (nameField.getText().isEmpty()) {
            showError("Validation Error", "Project name is required");
            return false;
        }
        if (startDatePicker.getValue() == null) {
            showError("Validation Error", "Start date is required");
            return false;
        }
        if (!budgetField.getText().isEmpty()) {
            try {
                Double.parseDouble(budgetField.getText());
            } catch (NumberFormatException e) {
                showError("Validation Error", "Budget must be a valid number");
                return false;
            }
        }
        if (!hourlyRateField.getText().isEmpty()) {
            try {
                Double.parseDouble(hourlyRateField.getText());
            } catch (NumberFormatException e) {
                showError("Validation Error", "Hourly rate must be a valid number");
                return false;
            }
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
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
} 
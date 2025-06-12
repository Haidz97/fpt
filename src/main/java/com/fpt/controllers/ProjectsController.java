package com.fpt.controllers;

import com.fpt.models.Project;
import com.fpt.models.ProjectStatus;
import com.fpt.models.TimeEntry;
import com.fpt.services.ProjectService;
import com.fpt.services.TimeEntryService;
import com.fpt.services.WorkloadService;
import com.fpt.services.PdfExportService;
import com.fpt.utils.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ProjectsController {
    @FXML private TableView<Project> projectsTable;
    @FXML private TableColumn<Project, String> nameColumn;
    @FXML private TableColumn<Project, String> clientColumn;
    @FXML private TableColumn<Project, ProjectStatus> statusColumn;
    @FXML private TableColumn<Project, LocalDate> startDateColumn;
    @FXML private TableColumn<Project, LocalDate> deadlineColumn;
    @FXML private TableColumn<Project, Double> budgetColumn;
    @FXML private TableColumn<Project, Double> hourlyRateColumn;
    @FXML private ListView<String> workloadIssuesList;

    private ProjectService projectService;
    private TimeEntryService timeEntryService;
    private WorkloadService workloadService;
    private PdfExportService pdfExportService;
    private ObservableList<Project> projectsList;

    @FXML
    public void initialize() {
        projectService = new ProjectService(DatabaseManager.getInstance());
        timeEntryService = new TimeEntryService(DatabaseManager.getInstance());
        workloadService = new WorkloadService(DatabaseManager.getInstance());
        pdfExportService = new PdfExportService();
        setupTable();
        loadProjects();
    }

    private void setupTable() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        clientColumn.setCellValueFactory(new PropertyValueFactory<>("client"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        deadlineColumn.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        budgetColumn.setCellValueFactory(new PropertyValueFactory<>("budget"));
        hourlyRateColumn.setCellValueFactory(new PropertyValueFactory<>("hourlyRate"));

        // Форматирование дат
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        startDateColumn.setCellFactory(column -> new TableCell<Project, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(dateFormatter));
                }
            }
        });

        deadlineColumn.setCellFactory(column -> new TableCell<Project, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(dateFormatter));
                }
            }
        });

        // Форматирование бюджета
        budgetColumn.setCellFactory(column -> new TableCell<Project, Double>() {
            @Override
            protected void updateItem(Double budget, boolean empty) {
                super.updateItem(budget, empty);
                if (empty || budget == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.2f ₽", budget));
                }
            }
        });

        // Форматирование почасовой ставки
        hourlyRateColumn.setCellFactory(column -> new TableCell<Project, Double>() {
            @Override
            protected void updateItem(Double rate, boolean empty) {
                super.updateItem(rate, empty);
                if (empty || rate == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.2f ₽/час", rate));
                }
            }
        });
    }

    @FXML
    public void loadProjects() {
        projectsList = FXCollections.observableArrayList(projectService.getAllProjects());
        projectsTable.setItems(projectsList);
        updateWorkloadIssues();
    }

    private void updateWorkloadIssues() {
        List<TimeEntry> allTimeEntries = timeEntryService.getAllTimeEntries();
        List<String> issues = workloadService.checkWorkloadIssues(projectsList, allTimeEntries);
        workloadIssuesList.setItems(FXCollections.observableArrayList(issues));
    }

    @FXML
    public void openWorkloadSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/WorkloadSettings.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Настройки нагрузки");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // После закрытия окна настроек обновляем предупреждения
            updateWorkloadIssues();
        } catch (IOException e) {
            showError("Ошибка", "Не удалось открыть окно настроек: " + e.getMessage());
        }
    }

    @FXML
    public void handleAddProject() {
        showProjectForm(null);
    }

    @FXML
    public void editProject() {
        Project selectedProject = projectsTable.getSelectionModel().getSelectedItem();
        if (selectedProject != null) {
            showProjectForm(selectedProject);
        }
    }

    @FXML
    public void deleteProject() {
        Project selectedProject = projectsTable.getSelectionModel().getSelectedItem();
        if (selectedProject != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Подтверждение удаления");
            alert.setHeaderText("Удаление проекта");
            alert.setContentText("Вы уверены, что хотите удалить проект \"" + selectedProject.getName() + "\"?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                projectService.deleteProject(selectedProject.getId());
                loadProjects();
            }
        }
    }

    @FXML
    public void exportToPdf() {
        Project selectedProject = projectsTable.getSelectionModel().getSelectedItem();
        if (selectedProject != null) {
            try {
                pdfExportService.exportProject(selectedProject);
                showSuccess("Экспорт завершен", "Отчет сохранен успешно");
            } catch (Exception e) {
                showError("Ошибка экспорта", "Не удалось экспортировать отчет: " + e.getMessage());
            }
        } else {
            showError("Нет выбранного проекта", "Пожалуйста, выберите проект для экспорта.");
        }
    }

    private void showProjectForm(Project project) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ProjectForm.fxml"));
            Parent root = loader.load();

            ProjectFormController controller = loader.getController();
            controller.setProject(project);

            Stage stage = new Stage();
            stage.setTitle(project == null ? "Новый проект" : "Редактирование проекта");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadProjects();
        } catch (IOException e) {
            showError("Ошибка", "Не удалось загрузить форму проекта: " + e.getMessage());
        }
    }

    public void refreshProjectList() {
        loadProjects();
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
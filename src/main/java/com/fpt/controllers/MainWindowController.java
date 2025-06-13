package com.fpt.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.net.URL;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Node;

public class MainWindowController {

    @FXML
    private StackPane contentArea;

    @FXML
    private void initialize() {
        showDashboard(); // По умолчанию показываем дашборд
    }

    @FXML
    private void showDashboard() {
        loadView("Dashboard.fxml");
    }

    @FXML
    private void showProjects() {
        loadView("Projects.fxml");
    }

    @FXML
    private void showWorkloadMonitor() {
        loadView("WorkloadMonitor.fxml");
    }

    @FXML
    private void showCalendar() {
        // TODO: Реализовать отображение календаря
    }

    @FXML
    private void showStatistics() {
        // TODO: Реализовать отображение статистики
    }

    @FXML
    private void showReports() {
        // TODO: Реализовать отображение отчетов
    }

    @FXML
    private void showSettings() {
        // TODO: Реализовать отображение настроек
    }

    @FXML
    private void syncWithUpwork() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Синхронизация с Upwork");
        alert.setHeaderText(null);
        alert.setContentText("Функция синхронизации с Upwork находится в разработке.");
        alert.showAndWait();
    }

    @FXML
    private void showImportDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ImportDialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Импорт проектов");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(contentArea.getScene().getWindow());

            ImportDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            // После закрытия диалога обновляем текущее представление
            String currentView = contentArea.getChildren().isEmpty() ? "Dashboard.fxml" :
                ((Node) contentArea.getChildren().get(0)).getId() + ".fxml";
            loadView(currentView);

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Не удалось открыть диалог импорта");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void loadView(String fxmlFile) {
        try {
            System.out.println("Loading view: " + fxmlFile); // Debug log
            URL fxmlUrl = getClass().getResource("/fxml/" + fxmlFile);
            if (fxmlUrl == null) {
                System.err.println("FXML file not found: " + fxmlFile); // Debug log
                throw new IOException("Cannot find FXML file: " + fxmlFile);
            }
            System.out.println("FXML URL: " + fxmlUrl); // Debug log
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            System.err.println("Error loading view " + fxmlFile + ": " + e.getMessage()); // Debug log
            e.printStackTrace();
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка загрузки");
            alert.setHeaderText(null);
            alert.setContentText("Не удалось загрузить " + fxmlFile + ": " + e.getMessage());
            alert.showAndWait();
        }
    }
} 
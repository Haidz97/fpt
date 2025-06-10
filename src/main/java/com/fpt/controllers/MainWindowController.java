package com.fpt.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.net.URL;

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
        // TODO: Реализовать синхронизацию с Upwork
    }

    private void loadView(String fxml) {
        try {
            URL resource = getClass().getResource("/fxml/" + fxml);
            if (resource == null) {
                throw new IOException("FXML file not found: " + fxml);
            }
            Parent view = FXMLLoader.load(resource);
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: Показать сообщение об ошибке пользователю
        }
    }
} 
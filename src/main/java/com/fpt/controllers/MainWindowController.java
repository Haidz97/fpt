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

    private void loadView(String fxmlFile) {
        try {
            URL fxmlUrl = getClass().getResource("/fxml/" + fxmlFile);
            if (fxmlUrl == null) {
                System.err.println("FXML file not found: /fxml/" + fxmlFile);
                // Попробуем альтернативный путь
                fxmlUrl = getClass().getResource("/com/fpt/" + fxmlFile);
                if (fxmlUrl == null) {
                    throw new IOException("Cannot find FXML file: " + fxmlFile);
                }
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent view = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading view: " + e.getMessage());
        }
    }
} 
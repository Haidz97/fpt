package com.fpt.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

public class MainWindowController {

    @FXML
    private StackPane contentArea;

    @FXML
    private void showDashboard() {
        // TODO: Реализовать отображение дашборда
    }

    @FXML
    private void showProjects() {
        // TODO: Реализовать отображение списка проектов
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

    @FXML
    private void initialize() {
        // Инициализация при запуске
        showDashboard(); // По умолчанию показываем дашборд
    }
}
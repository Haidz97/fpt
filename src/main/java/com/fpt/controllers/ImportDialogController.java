package com.fpt.controllers;

import com.fpt.services.ImportService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

public class ImportDialogController {
    @FXML private ComboBox<String> platformComboBox;
    @FXML private TextField apiKeyField;
    @FXML private TextField filePathField;
    @FXML private CheckBox importCompletedCheckBox;
    @FXML private CheckBox updateExistingCheckBox;
    @FXML private CheckBox importTimeEntriesCheckBox;

    private Stage dialogStage;
    private ImportService importService;
    private File selectedFile;

    @FXML
    private void initialize() {
        importService = new ImportService();
        platformComboBox.getSelectionModel().selectFirst();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void handleBrowse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл для импорта");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("CSV файлы", "*.csv"),
            new FileChooser.ExtensionFilter("Excel файлы", "*.xlsx", "*.xls")
        );

        File file = fileChooser.showOpenDialog(dialogStage);
        if (file != null) {
            selectedFile = file;
            filePathField.setText(file.getPath());
        }
    }

    @FXML
    private void handleImport() {
        String platform = platformComboBox.getValue();
        String apiKey = apiKeyField.getText();
        boolean importCompleted = importCompletedCheckBox.isSelected();
        boolean updateExisting = updateExistingCheckBox.isSelected();
        boolean importTimeEntries = importTimeEntriesCheckBox.isSelected();

        try {
            if (selectedFile != null) {
                // Импорт из файла
                importService.importFromFile(selectedFile, platform, importCompleted, 
                    updateExisting, importTimeEntries);
            } else if (!apiKey.isEmpty()) {
                // Импорт через API
                importService.importFromApi(platform, apiKey, importCompleted, 
                    updateExisting, importTimeEntries);
            } else {
                showError("Ошибка", "Выберите файл или введите API ключ");
                return;
            }

            showSuccess("Импорт завершен", "Проекты успешно импортированы");
            dialogStage.close();

        } catch (Exception e) {
            showError("Ошибка импорта", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 
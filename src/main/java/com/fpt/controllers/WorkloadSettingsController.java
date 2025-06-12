package com.fpt.controllers;

import com.fpt.models.WorkloadSettings;
import com.fpt.services.WorkloadService;
import com.fpt.utils.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * Контроллер формы настроек планирования нагрузки.
 * Позволяет пользователю настраивать:
 * - Ограничения по времени (дневные и недельные)
 * - Желаемое количество проектов
 * - Параметры уведомлений
 */
public class WorkloadSettingsController {
    /** Спиннер для выбора максимальной дневной нагрузки (1-24 часа) */
    @FXML private Spinner<Double> maxDailyHoursSpinner;
    
    /** Спиннер для выбора максимальной недельной нагрузки (1-168 часов) */
    @FXML private Spinner<Double> maxWeeklyHoursSpinner;
    
    /** Спиннер для выбора желаемого количества проектов (1-20) */
    @FXML private Spinner<Integer> desiredProjectCountSpinner;
    
    /** Спиннер для выбора количества дней для предупреждения о дедлайне (1-30) */
    @FXML private Spinner<Integer> deadlineWarningDaysSpinner;
    
    /** Чекбокс для включения/отключения уведомлений о дедлайнах */
    @FXML private CheckBox deadlineNotificationsCheckBox;
    
    /** Чекбокс для включения/отключения уведомлений о превышении нагрузки */
    @FXML private CheckBox workloadNotificationsCheckBox;
    
    /** Чекбокс для включения/отключения уведомлений о конфликтах в расписании */
    @FXML private CheckBox scheduleConflictNotificationsCheckBox;

    private WorkloadService workloadService;
    private WorkloadSettings settings;

    /**
     * Инициализирует форму настроек.
     * Загружает текущие настройки и настраивает элементы управления.
     */
    @FXML
    public void initialize() {
        workloadService = new WorkloadService(DatabaseManager.getInstance());
        settings = workloadService.getSettings();

        // Настройка спиннеров с ограничениями значений
        maxDailyHoursSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1, 24, settings.getMaxDailyHours(), 0.5));
        maxWeeklyHoursSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1, 168, settings.getMaxWeeklyHours(), 1));
        desiredProjectCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, settings.getDesiredProjectCount()));
        deadlineWarningDaysSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, settings.getDeadlineWarningDays()));

        // Установка текущих значений для чекбоксов
        deadlineNotificationsCheckBox.setSelected(settings.isEnableDeadlineNotifications());
        workloadNotificationsCheckBox.setSelected(settings.isEnableWorkloadNotifications());
        scheduleConflictNotificationsCheckBox.setSelected(settings.isEnableScheduleConflictNotifications());

        // Добавляем валидацию: дневная нагрузка не может быть больше недельной
        maxDailyHoursSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue > maxWeeklyHoursSpinner.getValue()) {
                maxDailyHoursSpinner.getValueFactory().setValue(oldValue);
            }
        });

        maxWeeklyHoursSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue < maxDailyHoursSpinner.getValue()) {
                maxWeeklyHoursSpinner.getValueFactory().setValue(oldValue);
            }
        });
    }

    /**
     * Обработчик нажатия кнопки "Сохранить".
     * Проверяет корректность введенных данных и сохраняет настройки.
     */
    @FXML
    public void handleSave() {
        if (validateInput()) {
            settings.setMaxDailyHours(maxDailyHoursSpinner.getValue());
            settings.setMaxWeeklyHours(maxWeeklyHoursSpinner.getValue());
            settings.setDesiredProjectCount(desiredProjectCountSpinner.getValue());
            settings.setDeadlineWarningDays(deadlineWarningDaysSpinner.getValue());
            settings.setEnableDeadlineNotifications(deadlineNotificationsCheckBox.isSelected());
            settings.setEnableWorkloadNotifications(workloadNotificationsCheckBox.isSelected());
            settings.setEnableScheduleConflictNotifications(scheduleConflictNotificationsCheckBox.isSelected());

            workloadService.saveSettings(settings);
            closeWindow();
        }
    }

    /**
     * Обработчик нажатия кнопки "Отмена".
     * Закрывает окно без сохранения изменений.
     */
    @FXML
    public void handleCancel() {
        closeWindow();
    }

    /**
     * Проверяет корректность введенных данных.
     * В настоящее время проверяет только соотношение дневной и недельной нагрузки.
     *
     * @return true если все данные корректны, false если есть ошибки
     */
    private boolean validateInput() {
        if (maxDailyHoursSpinner.getValue() > maxWeeklyHoursSpinner.getValue()) {
            showError("Ошибка валидации", "Дневная нагрузка не может быть больше недельной");
            return false;
        }
        return true;
    }

    /**
     * Отображает диалоговое окно с сообщением об ошибке.
     *
     * @param title заголовок окна
     * @param content текст сообщения об ошибке
     */
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Закрывает окно настроек.
     */
    private void closeWindow() {
        Stage stage = (Stage) maxDailyHoursSpinner.getScene().getWindow();
        stage.close();
    }
} 
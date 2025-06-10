package com.fpt.models;

import java.time.LocalDateTime;
import javafx.scene.paint.Color;

public class WorkloadAlert {
    public static final double MAX_DAILY_HOURS = 8.0;
    public static final double WARNING_THRESHOLD = 7.0;
    public static final double CRITICAL_THRESHOLD = 10.0;

    private final LocalDateTime date;
    private final double hoursWorked;
    private final AlertLevel level;
    private final String message;

    public enum AlertLevel {
        NORMAL(Color.GREEN),
        WARNING(Color.ORANGE),
        CRITICAL(Color.RED);

        private final Color color;

        AlertLevel(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return color;
        }
    }

    public WorkloadAlert(LocalDateTime date, double hoursWorked) {
        this.date = date;
        this.hoursWorked = hoursWorked;
        
        if (hoursWorked >= CRITICAL_THRESHOLD) {
            this.level = AlertLevel.CRITICAL;
            this.message = String.format("Критическая перегрузка! %.1f часов (превышение на %.1f ч)", 
                hoursWorked, hoursWorked - MAX_DAILY_HOURS);
        } else if (hoursWorked >= WARNING_THRESHOLD) {
            this.level = AlertLevel.WARNING;
            this.message = String.format("Приближение к лимиту: %.1f часов (до перегрузки %.1f ч)", 
                hoursWorked, MAX_DAILY_HOURS - hoursWorked);
        } else {
            this.level = AlertLevel.NORMAL;
            this.message = String.format("Нормальная нагрузка: %.1f часов", hoursWorked);
        }
    }

    public LocalDateTime getDate() {
        return date;
    }

    public double getHoursWorked() {
        return hoursWorked;
    }

    public AlertLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public Color getColor() {
        return level.getColor();
    }

    public double getRemainingHours() {
        return Math.max(0, MAX_DAILY_HOURS - hoursWorked);
    }
} 
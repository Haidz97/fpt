package com.fpt.models;

public enum ProjectSource {
    FREELANCE_RU("Freelance.ru"),
    FL_RU("FL.ru"),
    DIRECT("Прямой клиент"),
    OTHER("Другое");

    private final String displayName;

    ProjectSource(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
} 
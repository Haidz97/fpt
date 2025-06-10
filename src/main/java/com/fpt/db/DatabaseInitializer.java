package com.fpt.db;

public class DatabaseInitializer {
    public static void main(String[] args) {
        System.out.println("Initializing database...");
        DatabaseManager dbManager = DatabaseManager.getInstance();
        System.out.println("Database initialized successfully!");
        System.out.println("Database location: " + System.getProperty("user.home") + "/.fpt/fpt.db");
        dbManager.closeConnection();
    }
} 
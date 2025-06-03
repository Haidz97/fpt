package com.fpt;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            URL mainFxmlUrl = getClass().getResource("/fxml/MainWindow.fxml");
            if (mainFxmlUrl == null) {
                throw new IOException("Cannot find MainWindow.fxml");
            }

            FXMLLoader loader = new FXMLLoader(mainFxmlUrl);
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 800);

            primaryStage.setTitle("Freelance Project Tracker");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 
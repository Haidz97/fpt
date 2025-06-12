package com.fpt.utils;

import javafx.fxml.FXMLLoader;
import java.io.IOException;
import java.net.URL;

public class FXMLUtils {
    public static FXMLLoader loadFXML(String fxml) {
        try {
            URL location = FXMLUtils.class.getResource("/fxml/" + fxml);
            if (location == null) {
                throw new IOException("Cannot find FXML file: " + fxml);
            }
            FXMLLoader loader = new FXMLLoader(location);
            return loader;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load FXML: " + fxml, e);
        }
    }
} 
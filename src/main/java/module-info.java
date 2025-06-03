module com.example.fpt {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;
    requires java.sql;
//    requires kernel;
//    requires layout;
    requires kernel;
    requires layout;
    


    opens fxml to javafx.fxml, javafx.graphics;
    opens com.fpt.controllers to javafx.fxml;
    opens com.fpt.models to javafx.base;
    
//    exports com.fpt to javafx.graphics;
    exports com.fpt;
    exports com.fpt.controllers;
//    exports com.fpt.db to javafx.graphics;
    exports com.fpt.models;
    exports com.fpt.db;
    exports com.fpt.services;

}
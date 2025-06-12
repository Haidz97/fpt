module com.fpt {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;
    requires javafx.media;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires kernel;
    requires layout;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    opens com.fpt to javafx.fxml;
    opens com.fpt.controllers to javafx.fxml;
    opens com.fpt.models to javafx.base;
    
    exports com.fpt;
    exports com.fpt.controllers;
    exports com.fpt.models;
    exports com.fpt.services;
    exports com.fpt.utils;
}
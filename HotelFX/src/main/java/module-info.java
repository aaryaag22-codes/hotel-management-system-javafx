/**
 * module-info.java
 *
 * This file tells Java's module system which packages your app needs.
 * It is REQUIRED when using javafx.fxml (Scene Builder files).
 * Without this, FXMLLoader cannot access your LoginController.
 */
module com.hotel {
    // JavaFX modules we use
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    // Allow FXML/Scene Builder to access our controller package
    // "opens" means reflection access is allowed (FXMLLoader needs this)
    opens com.hotel.ui to javafx.fxml;
    opens com.hotel.model to javafx.base;

    // Export our main package
    exports com.hotel;
    exports com.hotel.ui;
    exports com.hotel.model;
    exports com.hotel.data;
    exports com.hotel.service;
}

package com.hotel;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * App.java — Entry point of the Hotel Management System
 *
 * How Scene Builder / FXML fits in:
 *   1. We use FXMLLoader to load login.fxml (which was created with Scene Builder)
 *   2. FXMLLoader reads the fxml file and automatically:
 *        a) Builds all the JavaFX nodes declared in the fxml
 *        b) Injects them into LoginController via @FXML annotations
 *        c) Wires up event handlers like onAction="#handleLogin"
 *   3. The rest of the app (tabs) is built in pure Java code — both approaches work!
 *
 * This demonstrates BOTH ways of building JavaFX UIs:
 *   → FXML + Scene Builder  (login screen)
 *   → Programmatic Java     (all main tabs)
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // ── Load the FXML login screen (Scene Builder file) ──────────────
        FXMLLoader loader = new FXMLLoader(
        getClass().getResource("/login.fxml")
);

        StackPane loginRoot = loader.load();
        // LoginController is automatically instantiated by FXMLLoader

        // Create the login scene
        Scene loginScene = new Scene(loginRoot, 900, 600);

        // Fade in the login screen on launch
        loginRoot.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(700), loginRoot);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        primaryStage.setTitle("🏨  Grand Vista Hotel — Login");
        primaryStage.setScene(loginScene);
        primaryStage.setResizable(true);   // login screen is fixed size
        primaryStage.show();

        fadeIn.play();  // start fade-in after window appears
    }

    public static void main(String[] args) {
        launch(args);
    }
}
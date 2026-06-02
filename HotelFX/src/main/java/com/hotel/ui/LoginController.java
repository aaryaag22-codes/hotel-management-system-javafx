package com.hotel.ui;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * LoginController.java
 *
 * This is the CONTROLLER for login.fxml — the file designed with Scene Builder.
 *
 * How Scene Builder works with JavaFX:
 *   1. You design the UI visually in Scene Builder → it generates login.fxml
 *   2. Each fx:id in the fxml maps to a @FXML field here
 *   3. Each onAction="#handleLogin" maps to a @FXML method here
 *   4. FXMLLoader links the two together at runtime
 *
 * Demonstrates: Week 9 JavaFX event handling, Scene switching
 */
public class LoginController {

    // ── @FXML fields are auto-injected from login.fxml fx:id attributes ──
    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;
    @FXML private Button        loginButton;

    // Hardcoded credentials (in real app would use a DB / file)
    private static final String VALID_USER = "admin";
    private static final String VALID_PASS = "admin123";

    /**
     * Called automatically by FXMLLoader after all @FXML fields are injected.
     * Use this like a constructor for the controller.
     */
    @FXML
    public void initialize() {
        // Allow pressing Enter in password field to trigger login
        passwordField.setOnAction(e -> handleLogin());

        // Hover effect on login button (since Scene Builder inline style
        // doesn't support pseudo-class, we add it here)
        loginButton.setOnMouseEntered(e -> loginButton.setStyle(
            "-fx-background-color: linear-gradient(to right, #E0C060, #C9A84C);"
          + "-fx-text-fill: #1A1A2E; -fx-font-weight: bold; -fx-font-size: 14px;"
          + "-fx-padding: 12 0; -fx-background-radius: 8; -fx-cursor: hand;"
          + "-fx-effect: dropshadow(gaussian, #C9A84C99, 16, 0, 0, 4);"
        ));
        loginButton.setOnMouseExited(e -> loginButton.setStyle(
            "-fx-background-color: linear-gradient(to right, #C9A84C, #A07830);"
          + "-fx-text-fill: #1A1A2E; -fx-font-weight: bold; -fx-font-size: 14px;"
          + "-fx-padding: 12 0; -fx-background-radius: 8; -fx-cursor: hand;"
          + "-fx-effect: dropshadow(gaussian, #C9A84C66, 10, 0, 0, 3);"
        ));
    }

    /**
     * Mapped to onAction="#handleLogin" in the FXML.
     * Called when the Sign In button is clicked.
     */
    @FXML
    public void handleLogin() {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            showError("Please enter both username and password.");
            shakeField(user.isEmpty() ? usernameField : passwordField);
            return;
        }

        if (VALID_USER.equals(user) && VALID_PASS.equals(pass)) {
            // ── Login successful → open main app ────────────────────────
            openMainApp();
        } else {
            showError("Invalid credentials. Try admin / admin123");
            shakeField(passwordField);
            passwordField.clear();
        }
    }

    private void showError(String message) {
        errorLabel.setText("⚠  " + message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);

        // Fade in the error label
        FadeTransition ft = new FadeTransition(Duration.millis(300), errorLabel);
        ft.setFromValue(0); ft.setToValue(1);
        ft.play();
    }

    /**
     * Shake animation — gives visual feedback on wrong password.
     * Demonstrates JavaFX Animation (bonus feature).
     */
    private void shakeField(javafx.scene.Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(60), node);
        tt.setFromX(0); tt.setByX(10);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.setOnFinished(e -> node.setTranslateX(0));
        tt.play();
    }

    /**
     * Fade out the login screen, then open the main hotel management window.
     */
    private void openMainApp() {
        // Get current stage from any node
        Stage loginStage = (Stage) loginButton.getScene().getWindow();

        // Fade out animation before switching
        FadeTransition fadeOut = new FadeTransition(
                Duration.millis(400), loginStage.getScene().getRoot());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            try {
                // Build main app scene
                MainLayout mainLayout = new MainLayout();
                Scene mainScene = new Scene(mainLayout.getRoot(), 1200, 780);

                // Fade in the new scene
                mainLayout.getRoot().setOpacity(0);
                loginStage.setScene(mainScene);
                loginStage.setTitle("🏨  Grand Vista Hotel Management System");
                loginStage.setMinWidth(1100);
                loginStage.setMinHeight(700);
                loginStage.setResizable(true);
                loginStage.setMaximized(true);  // Opens fullscreen/maximized

                FadeTransition fadeIn = new FadeTransition(
                        Duration.millis(500), mainLayout.getRoot());
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();

            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Failed to load main application.");
            }
        });
        fadeOut.play();
    }
}
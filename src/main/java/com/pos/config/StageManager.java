package com.pos.config;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

/**
 * Manages the JavaFX stages and scenes.
 * Handles navigation between views and applies transitions.
 */
@Component
public class StageManager {

    private final ApplicationContext applicationContext;
    private Stage primaryStage;

    @Value("${app.ui.animation-speed:300}")
    private int animationSpeed;

    /**
     * Constructor with dependencies.
     *
     * @param applicationContext The Spring application context
     */
    public StageManager(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Sets the primary stage for the application.
     *
     * @param primaryStage The primary stage
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Switches to a new scene.
     *
     * @param viewName The name of the view to switch to
     */
    public void switchScene(String viewName) {
        try {
            // Load the FXML file
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(viewName));

            // Set the controller factory to use Spring beans
            fxmlLoader.setControllerFactory(applicationContext::getBean);

            // Load the root element
            Parent root = fxmlLoader.load();

            // Create new scene
            Scene scene = new Scene(root);

            // Apply CSS theme
            applyTheme(scene);

            // Set the scene on the stage
            if (primaryStage.getScene() == null) {
                // First load, no transition needed
                primaryStage.setScene(scene);
                primaryStage.show();
            } else {
                // Animate transition to new scene
                animateSceneChange(scene);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar la vista: " + viewName, e);
        }
    }

    /**
     * Animates the transition between scenes.
     *
     * @param newScene The new scene to transition to
     */
    private void animateSceneChange(Scene newScene) {
        // Create fade out transition for current scene
        FadeTransition fadeOut = new FadeTransition(Duration.millis(animationSpeed),
                primaryStage.getScene().getRoot());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        // Set action to perform after fade out
        fadeOut.setOnFinished(event -> {
            // Set new scene
            primaryStage.setScene(newScene);

            // Create fade in transition
            FadeTransition fadeIn = new FadeTransition(Duration.millis(animationSpeed), newScene.getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        // Start fade out
        fadeOut.play();
    }

    /**
     * Applies the theme to the scene.
     *
     * @param scene The scene to apply the theme to
     */
    private void applyTheme(Scene scene) {
        // Load CSS theme
        // For now, we're using a default light theme
        scene.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/static/css/light-theme.css")).toExternalForm());
    }

    /**
     * Shows an error dialog.
     *
     * @param title The dialog title
     * @param message The error message
     */
    public void showErrorDialog(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    /**
     * Shows a confirmation dialog.
     *
     * @param title The dialog title
     * @param message The confirmation message
     * @return True if confirmed, false otherwise
     */
    public boolean showConfirmationDialog(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(primaryStage);

        return alert.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL) == javafx.scene.control.ButtonType.OK;
    }

    /**
     * Shows an information dialog.
     *
     * @param title The dialog title
     * @param message The information message
     */
    public void showInfoDialog(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    /**
     * Gets the primary stage.
     *
     * @return The primary stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }
}
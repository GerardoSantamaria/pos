package com.pos.manager.core;


import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

import static com.pos.util.ResourcePathChecker.getExistingResources;

/**
 * Manages the JavaFX stages and scenes.
 * Handles navigation between views and applies transitions.
 */
@Component
public class StageManager {

    private final ApplicationContext applicationContext;
    private final CssManager cssManager;
    private Stage primaryStage;

    @Value("${app.ui.animation-speed:300}")
    private int animationSpeed;

    /**
     * Constructor with dependencies.
     *
     * @param applicationContext The Spring application context
     */
    public StageManager(ApplicationContext applicationContext,
                        CssManager cssManager) {
        this.applicationContext = applicationContext;
        this.cssManager = cssManager;
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
            // Debug: Check if the resource exists
            boolean resourceExists = com.pos.util.ResourcePathChecker.resourceExists(viewName);
            if (!resourceExists) {
                System.err.println("!!! ERROR: FXML resource not found: " + viewName);
                System.err.println("Checking alternate paths...");

                // Try to find the resource with alternate paths
                String[] alternatePaths = {
                        viewName.replace("/product/", "/"),
                        viewName.replace("/product/", "-"),
                        viewName.replace("/product/", "/product-"),
                        viewName
                };
                com.pos.util.ResourcePathChecker.printResourceDebugInfo("FXML", alternatePaths);

                // If we can't find the resource, throw an exception
                if (!getExistingResources(alternatePaths).isEmpty()) {
                    String foundPath = getExistingResources(alternatePaths).get(0);
                    System.out.println("Found alternative path: " + foundPath);
                    viewName = foundPath;
                } else {
                    throw new IOException("Resource not found: " + viewName);
                }
            }

            // Load the FXML file
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(viewName));

            // Set the controller factory to use Spring beans
            fxmlLoader.setControllerFactory(applicationContext::getBean);

            // Load the root element
            Parent root = fxmlLoader.load();

            // Create new scene
            Scene scene = new Scene(root);

            // Apply CSS theme
            //applyTheme(scene);

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
        try {
            // Load CSS theme
            //String cssPath = ViewConfiguration.CSS_default;
            //String cssResource = getClass().getResource(cssPath).toExternalForm();
            scene.getStylesheets().add(this.cssManager.getCurrentTheme());
        } catch (Exception e) {
            System.err.println("Error loading theme: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Shows an error dialog.
     *
     * @param title The dialog title
     * @param message The error message
     */
    public void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
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
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(primaryStage);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Shows an information dialog.
     *
     * @param title The dialog title
     * @param message The information message
     */
    public void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
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
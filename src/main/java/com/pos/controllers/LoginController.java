package com.pos.controllers;

import com.pos.manager.StageManager;
import com.pos.config.ViewConfiguration;
import com.pos.services.AuthService;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the login view.
 */
@Controller
public class LoginController implements Initializable {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    private final AuthService authService;
    private final StageManager stageManager;

    @Value("${app.ui.animation-speed:300}")
    private int animationSpeed;

    /**
     * Constructor with dependencies.
     *
     * @param authService The authentication service
     * @param stageManager The stage manager
     */
    public LoginController(AuthService authService, StageManager stageManager) {
        this.authService = authService;
        this.stageManager = stageManager;
    }

    /**
     * Initializes the controller.
     *
     * @param location The location used to resolve relative paths
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Clear error label
        errorLabel.setText("");

        // Apply fade-in animation to root pane
        FadeTransition fadeIn = new FadeTransition(Duration.millis(animationSpeed), rootPane);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        // Set focus to username field
        usernameField.requestFocus();

        // Add enter key handler to password field
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                handleLogin(new ActionEvent());
            }
        });
    }

    /**
     * Handles the login button action.
     *
     * @param event The action event
     */
    @FXML
    public void handleLogin(ActionEvent event) {
        // Clear error message
        errorLabel.setText("");

        // Get credentials
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            showError("Por favor ingrese usuario y contraseña");
            return;
        }

        try {
            // Attempt authentication
            authService.authenticate(username, password);

            // On success, transition to dashboard
            FadeTransition fadeOut = new FadeTransition(Duration.millis(animationSpeed), rootPane);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> stageManager.switchScene(ViewConfiguration.DASHBOARD_VIEW));
            fadeOut.play();

        } catch (BadCredentialsException e) {
            showError("Credenciales inválidas");
        } catch (UsernameNotFoundException e) {
            showError("Usuario no encontrado");
        } catch (Exception e) {
            showError("Error de autenticación: " + e.getMessage());
        }
    }

    /**
     * Shows an error message.
     *
     * @param message The error message
     */
    private void showError(String message) {
        errorLabel.setText(message);

        // Shake animation for the login button
        javafx.animation.TranslateTransition shake = new javafx.animation.TranslateTransition(Duration.millis(100), loginButton);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.play();
    }
}
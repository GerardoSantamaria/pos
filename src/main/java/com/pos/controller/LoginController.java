package com.pos.controller;

import com.pos.service.AuthenticationService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class LoginController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    private final AuthenticationService authService;
    private final ApplicationContext applicationContext;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    // Constructor con inyección de dependencias en lugar de @RequiredArgsConstructor
    public LoginController(AuthenticationService authService, ApplicationContext applicationContext) {
        this.authService = authService;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        // Configurar eventos adicionales
        passwordField.setOnAction(this::handleLogin);

        // Limpiar el mensaje de error al cambiar los datos
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> errorLabel.setVisible(false));
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> errorLabel.setVisible(false));
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validación básica
        if (username.isEmpty() || password.isEmpty()) {
            showError("El usuario y la contraseña son obligatorios");
            return;
        }

        // Deshabilitar el botón durante el intento de login
        loginButton.setDisable(true);

        try {
            boolean authenticated = authService.authenticate(username, password);

            if (authenticated) {
                // Login exitoso - abrir la pantalla principal
                openMainScreen();
            } else {
                showError("Usuario o contraseña incorrectos");
            }
        } catch (Exception e) {
            LOGGER.error("Error durante el login", e);
            showError("Error al iniciar sesión: " + e.getMessage());
        } finally {
            loginButton.setDisable(false);
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);

        // Agitar el campo de contraseña para indicar error
        animateErrorShake(passwordField);
    }

    private void animateErrorShake(javafx.scene.Node node) {
        // Esta función implementaría una animación de "shake" para mejorar UX
        // Usando javafx.animation.TranslateTransition
        // Por simplicidad, no se implementa completamente aquí
    }

    private void openMainScreen() {
        try {
            // Cargar el archivo FXML del POS
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/templates/pos.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            // Obtener el stage actual
            Stage stage = (Stage) loginButton.getScene().getWindow();

            // Crear nueva escena
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/static/css/styles.css").toExternalForm());

            // Configurar y mostrar
            stage.setScene(scene);
            stage.setTitle("Sistema POS");
            stage.setMaximized(true);
            stage.show();

            LOGGER.info("Pantalla principal cargada correctamente");
        } catch (Exception e) {
            LOGGER.error("Error al cargar la pantalla principal", e);
            showError("Error al cargar la aplicación: " + e.getMessage());
        }
    }
}
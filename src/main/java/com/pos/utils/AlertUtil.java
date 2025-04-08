package com.pos.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Clase de utilidades para mostrar diálogos y alertas al usuario
 */
public class AlertUtil {

    /**
     * Muestra un mensaje de información
     */
    public static void showInformation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Aplicar estilos
        styleAlert(alert);

        alert.showAndWait();
    }

    /**
     * Muestra un mensaje de advertencia
     */
    public static void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        // Aplicar estilos
        styleAlert(alert);

        alert.showAndWait();
    }

    /**
     * Muestra un mensaje de error
     */
    public static void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Aplicar estilos
        styleAlert(alert);

        alert.showAndWait();
    }

    /**
     * Muestra un mensaje de error con detalles expandibles (para excepciones)
     */
    public static void showErrorWithDetails(String title, String header, String content, String details) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Crear área de texto expandible para detalles
        TextArea textArea = new TextArea(details);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);

        alert.getDialogPane().setExpandableContent(expContent);

        // Aplicar estilos
        styleAlert(alert);

        alert.showAndWait();
    }

    /**
     * Muestra un diálogo de confirmación
     * @return true si el usuario confirma, false si cancela
     */
    public static boolean showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Aplicar estilos
        styleAlert(alert);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Aplica estilos y ajustes adicionales al diálogo de alerta
     */
    private static void styleAlert(Alert alert) {
        // Hacer que el diálogo sea redimensionable
        alert.setResizable(true);

        // Obtener el Stage para poder aplicar estilos adicionales
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setMinWidth(400);

        // Se podría aplicar una hoja de estilos CSS específica para alertas
        // alert.getDialogPane().getStylesheets().add("/static/css/alerts.css");
    }
}
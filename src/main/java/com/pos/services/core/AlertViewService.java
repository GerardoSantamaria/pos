package com.pos.services.core;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.springframework.stereotype.Service;

@Service
public class AlertViewService {

    public void showErrorDialog(String title, String message, Stage rootStage) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.initOwner(rootStage);
            alert.showAndWait();
        });
    }

    public void showInfoDialog(String title, String message, Stage rootStage) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.initOwner(rootStage);
            alert.showAndWait();
        });
    }
}

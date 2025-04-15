package com.pos.utils;

import atlantafx.base.theme.Styles;
import javafx.scene.control.Button;

public class ButtonFactory {

    public static Button createEliminateButton() {
        Button deleteButton = new Button("Eliminar");
        deleteButton.getStyleClass().add(Styles.WARNING);
        return deleteButton;
    }

    public static Button createEditButton() {
        Button editButton = new Button("Editar");
        editButton.getStyleClass().add(Styles.ACCENT);
        return editButton;
    }

    public static Button createActivateDeactivateButton() {
        Button deleteButton = new Button("Activat/Desactivar");
        deleteButton.getStyleClass().add(Styles.SUCCESS);
        return deleteButton;
    }
}

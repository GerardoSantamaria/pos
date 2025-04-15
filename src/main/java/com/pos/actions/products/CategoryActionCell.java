package com.pos.actions.products;

import com.pos.controllers.products.CategoryController;
import com.pos.dtos.products.CategoryDTO;
import com.pos.utils.ButtonFactory;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;

import java.util.Optional;

public class CategoryActionCell extends TableCell<CategoryDTO, CategoryDTO> {

    private final HBox buttonsContainer;
    private final CategoryController controller;

    public CategoryActionCell(CategoryController controller) {
        this.controller = controller;
        Button editButton = createEditButton();
        Button deleteButton = createActivateDeactivateButton();
        this.buttonsContainer = new HBox(5);
        this.buttonsContainer.getChildren().addAll(editButton, deleteButton);

    }

    private Button createActivateDeactivateButton() {
        Button deleteButton = ButtonFactory.createActivateDeactivateButton();
        deleteButton.setOnAction( event -> {
            Optional<CategoryDTO> optionalCategoryDTO = this.getCurrentCategory();
            optionalCategoryDTO.ifPresent(this.controller::activateDeactivateCategory);
        });
        return deleteButton;
    }

    private Button createEditButton() {
        Button editButton = ButtonFactory.createEditButton();
        editButton.setOnAction(event -> {
            Optional<CategoryDTO> optionalCategoryDTO = this.getCurrentCategory();
            optionalCategoryDTO.ifPresent(this.controller::openCategoryForm);
        });
        return editButton;
    }

    private Optional<CategoryDTO> getCurrentCategory() {
        CategoryDTO categoryDTO = (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) ?
                                        getTableView().getItems().get(getIndex()) :
                                        null;
        return Optional.ofNullable(categoryDTO);
    }

    @Override
    protected void updateItem(CategoryDTO category, boolean empty) {
        super.updateItem(category, empty);

        if (empty || category == null) {
            setGraphic(null);
            setText(null);
        } else {
            setGraphic(this.buttonsContainer);
            setText(null);
        }
    }
}

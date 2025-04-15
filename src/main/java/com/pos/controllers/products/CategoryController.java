package com.pos.controllers.products;


import atlantafx.base.theme.Tweaks;
import com.pos.actions.products.CategoryActionCell;
import com.pos.dtos.products.CategoryDTO;
import com.pos.services.products.CategoryService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Controller
public class CategoryController implements Initializable {

    @FXML
    private TableView<CategoryDTO> categoryTable;

    @FXML
    private TableColumn<CategoryDTO, String> nameColumn;

    @FXML
    private TableColumn<CategoryDTO, String> descriptionColumn;

    @FXML
    private TableColumn<CategoryDTO, CategoryDTO> actionColumn;

    private final ObservableList<CategoryDTO> categoryList = FXCollections.observableArrayList();
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.initializeTableCategory();
        this.loadCategory();
    }

    private void loadCategory() {
        List<CategoryDTO> categoryDTOList = this.categoryService.getAllCategories();
        categoryList.clear();
        categoryList.addAll(categoryDTOList);
    }

    private void initializeTableCategory() {
        this.nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        this.descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("descritpion"));
        this.actionColumn.getStyleClass().add(Tweaks.ALIGN_CENTER);
        this.actionColumn.setCellValueFactory(category -> new SimpleObjectProperty<>(category.getValue()));
        this.actionColumn.setCellFactory(getActionColumnCellFactory());
    }

    private Callback<TableColumn<CategoryDTO, CategoryDTO>, TableCell<CategoryDTO, CategoryDTO>> getActionColumnCellFactory() {
        return param -> new CategoryActionCell(this);
    }

    public void onSearch(ActionEvent actionEvent) {
    }

    public void onUpdate(ActionEvent actionEvent) {
    }

    public void onCreateCategory(ActionEvent actionEvent) {
    }

    public void openCategoryForm(CategoryDTO categoryDTO) {

    }

    public void activateDeactivateCategory(CategoryDTO categoryDTO) {

    }
}

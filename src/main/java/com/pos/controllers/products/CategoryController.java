package com.pos.controllers.products;

import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import com.pos.actions.products.CategoryActionCell;
import com.pos.config.ViewConfiguration;
import com.pos.dtos.products.CategoryDTO;
import com.pos.manager.core.StageManager;
import com.pos.services.core.AlertViewService;
import com.pos.services.core.ModalViewService;
import com.pos.services.products.CategoryService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

@Controller
public class CategoryController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryController.class);

    @FXML
    private BorderPane container;

    @FXML
    private TableView<CategoryDTO> categoryTable;

    @FXML
    private TableColumn<CategoryDTO, String> nameColumn;

    @FXML
    private TableColumn<CategoryDTO, String> descriptionColumn;

    @FXML
    private TableColumn<CategoryDTO, CategoryDTO> actionsColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    private String currentSearchTerm = "";
    private Stage stage;
    private final ObservableList<CategoryDTO> categoryList = FXCollections.observableArrayList();
    private final CategoryService categoryService;
    private final ModalViewService modalViewService;
    private final AlertViewService alertViewService;
    private final StageManager stageManager;

    public CategoryController(CategoryService categoryService,
                              ModalViewService modalViewService, AlertViewService alertViewService, StageManager stageManager) {
        this.categoryService = categoryService;
        this.modalViewService = modalViewService;
        this.alertViewService = alertViewService;
        this.stageManager = stageManager;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //stage = (Stage)this.container.getScene().getWindow();
        this.initializeTableCategory();
        this.initializeSearchText();
        this.loadCategory();
    }

    private void initializeSearchText() {
        this.searchField.setOnKeyPressed( event -> {
            if (Objects.equals(KeyCode.ENTER, event.getCode())) {
                this.onSearch(null);
            }
        });
    }

    private void loadCategory() {
        List<CategoryDTO> categoryDTOList = StringUtils.isEmpty(currentSearchTerm) ?
                                            this.categoryService.getAllCategories() :
                                            this.categoryService.getCategoriesByNameContaining(currentSearchTerm);
        categoryList.clear();
        categoryList.addAll(categoryDTOList);
    }

    private void initializeTableCategory() {
        this.categoryTable.setRowFactory(this.getCategoryTableFactory());
        this.nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        this.descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("descritpion"));
        this.actionsColumn.getStyleClass().add(Tweaks.ALIGN_CENTER);
        this.actionsColumn.setCellValueFactory(category -> new SimpleObjectProperty<>(category.getValue()));
        this.actionsColumn.setCellFactory(category -> new CategoryActionCell(this));
    }

    private Callback<TableView<CategoryDTO>, TableRow<CategoryDTO>> getCategoryTableFactory() {
        return tv -> {
            TableRow<CategoryDTO> categoryDTOTableRow = new TableRow<>();
            categoryDTOTableRow.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (Objects.nonNull(newItem) && !newItem.isActive()) {
                    categoryDTOTableRow.getStyleClass().addAll(Styles.TEXT_SUBTLE);
                }
            });
            return categoryDTOTableRow;
        };
    }

    public void onSearch(ActionEvent actionEvent) {
        this.currentSearchTerm =  this.searchField.getText().trim();
        loadCategory();
    }

    public void onUpdate(ActionEvent actionEvent) {
        this.onSearch(actionEvent);
    }

    public void onCreateCategory(ActionEvent actionEvent) {
        this.openCategoryForm(null);
    }

    public void openCategoryForm(CategoryDTO categoryDTO) {
        try {
            String titleModal = Objects.isNull(categoryDTO) ? "Crear categoria" : "Actualizar categoria";
            this.modalViewService.loadViewByPathAndParam(ViewConfiguration.CATEGORY_FORM_VIEW, titleModal, categoryDTO);
            this.loadCategory();
        } catch (IOException e) {
            this.alertViewService.showErrorDialog("Error", "Error al abrir el formulario de producto: " + e.getMessage(), this.stage);
            LOGGER.error("Error al abrir el formulario de producto", e);
        }
    }

    public void activateDeactivateCategory(CategoryDTO categoryDTO) {
        try {
            String title = categoryDTO.isActive() ? "Categoria desactivada" : "Categoria activa";
            this.categoryService.updateCategoryActiveById(categoryDTO.getId());
            this.loadCategory();
            this.alertViewService.showInfoDialog(title, "La categoria cambio de estado exitosamente", stage);
        } catch (Exception e) {
            this.alertViewService.showErrorDialog("Error", "Error al abrir el activar o desactivar categoria ", stage);
            LOGGER.error("Error en actualizar estado de categoria id: {}", categoryDTO.getId(), e);
        }
    }
}
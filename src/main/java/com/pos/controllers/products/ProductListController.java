package com.pos.controllers.products;

import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import com.pos.manager.core.StageManager;
import com.pos.config.ViewConfiguration;
import com.pos.models.products.Product;
import com.pos.services.core.AuthService;
import com.pos.services.products.ProductService;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ResourceBundle;

/**
 * Controller for the product list view.
 */
@Controller
public class ProductListController implements Initializable {

    @FXML
    private TableView<Product> productTable;

    @FXML
    private TableColumn<Product, Long> idColumn;

    @FXML
    private TableColumn<Product, String> barcodeColumn;

    @FXML
    private TableColumn<Product, String> nameColumn;

    @FXML
    private TableColumn<Product, BigDecimal> priceColumn;

    @FXML
    private TableColumn<Product, BigDecimal> costColumn;

    @FXML
    private TableColumn<Product, Integer> stockColumn;

    @FXML
    private TableColumn<Product, Product> actionsColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private Button addButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Pagination pagination;

    @FXML
    private ComboBox<String> sortByComboBox;

    @FXML
    private ComboBox<String> sortDirectionComboBox;

    private final ProductService productService;
    private final StageManager stageManager;
    private final AuthService authService;
    private final ApplicationContext applicationContext;

    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private int currentPage = 0;
    private String currentSortBy = "id";
    private boolean currentSortAscending = true;
    private String currentSearchTerm = "";

    @Value("${app.ui.page-size:20}")
    private int pageSize;

    /**
     * Constructor with dependencies.
     *
     * @param productService The product service
     * @param stageManager The stage manager
     * @param authService The authentication service
     * @param applicationContext The Spring application context
     */
    public ProductListController(
            ProductService productService,
            StageManager stageManager,
            AuthService authService,
            ApplicationContext applicationContext) {
        this.productService = productService;
        this.stageManager = stageManager;
        this.authService = authService;
        this.applicationContext = applicationContext;
    }

    /**
     * Initializes the controller.
     *
     * @param location The location used to resolve relative paths
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize table columns
        initializeTableColumns();

        // Initialize sorting options
        initializeSortOptions();

        // Set button actions
        addButton.setOnAction(event -> openProductForm(null));
        refreshButton.setOnAction(event -> loadProducts());
        searchButton.setOnAction(event -> search());

        // Enter key in search field triggers search
        searchField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                search();
            }
        });

        // Set up pagination
        pagination.setPageFactory(this::createPage);

        // Load initial data
        loadProducts();

        // Set button permissions
        setButtonPermissions();
    }

    /**
     * Initializes the table columns.
     */
    private void initializeTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Format price column
        priceColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPrice()));
        priceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(NumberFormat.getCurrencyInstance().format(price));
                }
            }
        });

        costColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCost()));
        costColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(NumberFormat.getCurrencyInstance().format(price));
                }
            }
        });

        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));

        // Configure actions column
        actionsColumn.getStyleClass().add(Tweaks.ALIGN_CENTER);
        actionsColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        actionsColumn.setCellFactory(getActionColumnCellFactory());

        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        // Set table items
        productTable.setItems(productList);
    }

    /**
     * Initializes the sorting options.
     */
    private void initializeSortOptions() {
        // Populate sort by combo box
        sortByComboBox.getItems().addAll("ID", "Código", "Nombre", "Precio", "Costo", "Stock");
        sortByComboBox.setValue("ID");
        sortByComboBox.setOnAction(event -> {
            switch (sortByComboBox.getValue()) {
                case "ID":
                    currentSortBy = "id";
                    break;
                case "Código":
                    currentSortBy = "barcode";
                    break;
                case "Nombre":
                    currentSortBy = "name";
                    break;
                case "Precio":
                    currentSortBy = "price";
                    break;
                case "Costo":
                    currentSortBy = "cost";
                    break;
                case "Stock":
                    currentSortBy = "stock";
                    break;
                default:
                    currentSortBy = "id";
                    break;
            }
            loadProducts();
        });

        // Populate sort direction combo box
        sortDirectionComboBox.getItems().addAll("Ascendente", "Descendente");
        sortDirectionComboBox.setValue("Ascendente");
        sortDirectionComboBox.setOnAction(event -> {
            currentSortAscending = "Ascendente".equals(sortDirectionComboBox.getValue());
            loadProducts();
        });
    }

    /**
     * Sets button permissions based on user role.
     */
    private void setButtonPermissions() {
        // Only admins and vendors can add products
        addButton.setDisable(!authService.hasRole("ROLE_ADMIN") && !authService.hasRole("ROLE_VENDEDOR"));
    }

    /**
     * Creates a page for the pagination control.
     *
     * @param pageIndex The page index
     * @return The page node
     */
    private TableView<Product> createPage(int pageIndex) {
        currentPage = pageIndex;
        loadProducts();
        return productTable;
    }

    /**
     * Loads products from the service.
     */
    public void loadProducts() {
        try {
            Page<Product> productPage;

            // Check if searching
            if (currentSearchTerm != null && !currentSearchTerm.trim().isEmpty()) {
                productPage = productService.searchProducts(currentSearchTerm, currentPage, pageSize);
            } else {
                productPage = productService.findAllPaginated(currentPage, pageSize, currentSortBy, currentSortAscending);
            }

            // Update pagination
            int totalPages = productPage.getTotalPages();
            pagination.setPageCount(totalPages > 0 ? totalPages : 1);
            pagination.setCurrentPageIndex(currentPage);

            // Update table
            productList.clear();
            productList.addAll(productPage.getContent());
        } catch (Exception e) {
            stageManager.showErrorDialog("Error", "Error al cargar los productos: " + e.getMessage());
        }
    }

    /**
     * Performs a search based on the search field text.
     */
    private void search() {
        currentSearchTerm = searchField.getText().trim();
        currentPage = 0;
        pagination.setCurrentPageIndex(0);
        loadProducts();
    }

    /**
     * Gets the cell factory for the action column.
     *
     * @return The cell factory
     */
    private Callback<TableColumn<Product, Product>, TableCell<Product, Product>> getActionColumnCellFactory() {
        return param -> new TableCell<>() {
            private final Button editButton = new Button("Editar");
            private final Button deleteButton = new Button("Eliminar");

            {
                // Configure edit button
                editButton.getStyleClass().add(Styles.WARNING);
                editButton.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    openProductForm(product);
                });

                // Configure delete button
                deleteButton.getStyleClass().add(Styles.DANGER);
                deleteButton.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    deleteProduct(product);
                });

                // Disable buttons for non-admin users
                if (!authService.hasRole("ROLE_ADMIN")) {
                    deleteButton.setDisable(true);
                }
            }

            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);

                if (empty || product == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    // Create button container
                    javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(5);
                    buttons.getChildren().addAll(editButton, deleteButton);
                    setGraphic(buttons);
                    setText(null);
                }
            }
        };
    }

    /**
     * Abre el formulario de producto para añadir o editar.
     * Modifica la implementación para que funcione dentro del dashboard.
     *
     * @param product El producto a editar, o null para crear uno nuevo
     */
    private void openProductForm(Product product) {
        try {
            // Load the product form FXML
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(ViewConfiguration.PRODUCT_FORM_VIEW));
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Parent formRoot = fxmlLoader.load();

            // Get the controller and set the product
            ProductFormController controller = fxmlLoader.getController();
            controller.setProduct(product);
            controller.setProductListController(this);

            // Create a new stage for the form
            Stage formStage = new Stage();
            formStage.initModality(Modality.APPLICATION_MODAL);
            formStage.initOwner(stageManager.getPrimaryStage());
            formStage.setTitle(product == null ? "Agregar Producto" : "Editar Producto");
            formStage.setScene(new Scene(formRoot));
            formStage.showAndWait();

        } catch (IOException e) {
            Platform.runLater(() -> {
                stageManager.showErrorDialog("Error", "Error al abrir el formulario de producto: " + e.getMessage());
            });
            System.err.println("Error al abrir el formulario de producto: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Deletes a product after confirmation.
     *
     * @param product The product to delete
     */
    private void deleteProduct(Product product) {
        // Confirm deletion using Platform.runLater to avoid threading issues
        Platform.runLater(() -> {
            boolean confirmed = stageManager.showConfirmationDialog(
                    "Eliminar Producto",
                    "¿Está seguro que desea eliminar el producto '" + product.getName() + "'?");

            if (confirmed) {
                try {
                    // Delete product
                    productService.deleteById(product.getId());

                    // Reload products
                    loadProducts();

                    // Show success message
                    Platform.runLater(() -> {
                        stageManager.showInfoDialog("Producto Eliminado",
                                "El producto ha sido eliminado exitosamente.");
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        stageManager.showErrorDialog("Error",
                                "Error al eliminar el producto: " + e.getMessage());
                    });
                }
            }
        });
    }
}
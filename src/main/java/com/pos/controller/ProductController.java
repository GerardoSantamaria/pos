package com.pos.controller;

import com.pos.models.Category;
import com.pos.models.Product;
import com.pos.service.BarcodeScannerService;
import com.pos.service.CategoryService;
import com.pos.service.ProductService;
import com.pos.utils.AlertUtil;
import com.pos.utils.FormatterUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

@Component
public class ProductController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BarcodeScannerService barcodeScanner;
    private final ApplicationContext applicationContext;

    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, String> skuColumn;
    @FXML private TableColumn<Product, String> barcodeColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, String> priceColumn;
    @FXML private TableColumn<Product, Integer> stockColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    @FXML private TableColumn<Product, Void> actionsColumn;

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button newProductButton;
    @FXML private Label totalProductsLabel;

    private ObservableList<Product> productsList = FXCollections.observableArrayList();
    private TextField barcodeFieldRef; // Para referencia en diálogo de producto

    // Constructor con inyección de dependencias en lugar de @RequiredArgsConstructor
    public ProductController(ProductService productService, CategoryService categoryService,
                             BarcodeScannerService barcodeScanner, ApplicationContext applicationContext) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.barcodeScanner = barcodeScanner;
        this.applicationContext = applicationContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar columnas de la tabla
        setupTableColumns();

        // Cargar datos iniciales
        loadAllProducts();

        // Configurar eventos
        searchField.setOnAction(this::searchProducts);
    }

    /**
     * Configura las columnas de la tabla de productos
     */
    private void setupTableColumns() {
        skuColumn.setCellValueFactory(new PropertyValueFactory<>("sku"));
        barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Formatear precio como moneda
        priceColumn.setCellValueFactory(data ->
                new SimpleStringProperty(FormatterUtil.formatCurrency(data.getValue().getPrice())));

        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));

        // Mostrar nombre de categoría
        categoryColumn.setCellValueFactory(data -> {
            Category category = data.getValue().getCategory();
            return new SimpleStringProperty(category != null ? category.getName() : "");
        });

        // Configurar columna de acciones
        setupActionsColumn();

        // Establecer fuente de datos
        productsTable.setItems(productsList);
    }

    /**
     * Configura la columna de acciones con botones
     */
    private void setupActionsColumn() {
        Callback<TableColumn<Product, Void>, TableCell<Product, Void>> cellFactory =
                new Callback<>() {
                    @Override
                    public TableCell<Product, Void> call(final TableColumn<Product, Void> param) {
                        return new TableCell<>() {
                            private final Button editBtn = new Button("Editar");
                            private final Button deleteBtn = new Button("Eliminar");

                            {
                                editBtn.getStyleClass().add("action-button");
                                deleteBtn.getStyleClass().add("danger-button");

                                editBtn.setOnAction(event -> {
                                    Product product = getTableView().getItems().get(getIndex());
                                    openEditProductDialog(product);
                                });

                                deleteBtn.setOnAction(event -> {
                                    Product product = getTableView().getItems().get(getIndex());
                                    confirmAndDeleteProduct(product);
                                });
                            }

                            private final HBox buttonsBox = new HBox(5, editBtn, deleteBtn);

                            @Override
                            protected void updateItem(Void item, boolean empty) {
                                super.updateItem(item, empty);

                                if (empty) {
                                    setGraphic(null);
                                } else {
                                    setGraphic(buttonsBox);
                                }
                            }
                        };
                    }
                };

        actionsColumn.setCellFactory(cellFactory);
    }

    /**
     * Carga todos los productos activos
     */
    private void loadAllProducts() {
        try {
            List<Product> products = productService.getAllActiveProducts();
            productsList.clear();
            productsList.addAll(products);
            updateTotalProductsLabel();
        } catch (Exception e) {
            log.error("Error al cargar productos", e);
            AlertUtil.showError("Error", "Error al cargar productos", e.getMessage());
        }
    }

    /**
     * Busca productos por nombre
     */
    @FXML
    public void searchProducts(ActionEvent event) {
        String searchTerm = searchField.getText().trim();

        if (searchTerm.isEmpty()) {
            loadAllProducts();
            return;
        }

        try {
            List<Product> foundProducts = productService.searchProductsByName(searchTerm);
            productsList.clear();
            productsList.addAll(foundProducts);
            updateTotalProductsLabel();
        } catch (Exception e) {
            log.error("Error al buscar productos", e);
            AlertUtil.showError("Error", "Error al buscar productos", e.getMessage());
        }
    }

    /**
     * Actualiza la etiqueta con el total de productos
     */
    private void updateTotalProductsLabel() {
        totalProductsLabel.setText("Total de productos: " + productsList.size());
    }

    /**
     * Abre el diálogo para crear un nuevo producto
     */
    @FXML
    public void openNewProductDialog() {
        try {
            // Crear un nuevo producto vacío
            Product newProduct = new Product();

            // Abrir el diálogo de edición
            boolean confirmed = showProductDialog(newProduct, "Nuevo Producto");

            if (confirmed) {
                // Guardar el nuevo producto
                Product savedProduct = productService.saveProduct(newProduct);

                // Actualizar la tabla
                productsList.add(savedProduct);
                updateTotalProductsLabel();

                // Mostrar mensaje de éxito
                AlertUtil.showInformation("Producto Creado", "Producto guardado correctamente",
                        "Se ha creado el producto: " + savedProduct.getName());
            }
        } catch (Exception e) {
            log.error("Error al crear nuevo producto", e);
            AlertUtil.showError("Error", "Error al crear producto", e.getMessage());
        }
    }

    /**
     * Abre el diálogo para editar un producto existente
     */
    private void openEditProductDialog(Product product) {
        try {
            // Abrir el diálogo de edición
            boolean confirmed = showProductDialog(product, "Editar Producto");

            if (confirmed) {
                // Guardar los cambios
                Product savedProduct = productService.saveProduct(product);

                // Actualizar la tabla
                int index = findProductIndexInList(product.getId());
                if (index >= 0) {
                    productsList.set(index, savedProduct);
                }

                // Mostrar mensaje de éxito
                AlertUtil.showInformation("Producto Actualizado", "Producto actualizado correctamente",
                        "Se ha actualizado el producto: " + savedProduct.getName());
            }
        } catch (Exception e) {
            log.error("Error al editar producto", e);
            AlertUtil.showError("Error", "Error al editar producto", e.getMessage());
        }
    }

    /**
     * Muestra el diálogo de producto para creación o edición
     */
    private boolean showProductDialog(Product product, String title) throws IOException {
        // Cargar el FXML del diálogo
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/templates/product_form.fxml"));
        DialogPane dialogPane = loader.load();

        // Configurar el diálogo
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setDialogPane(dialogPane);
        dialog.setTitle(title);

        // Obtener referencias a los componentes
        TextField nameField = (TextField) dialogPane.lookup("#nameField");
        TextField skuField = (TextField) dialogPane.lookup("#skuField");
        TextField barcodeField = (TextField) dialogPane.lookup("#barcodeField");
        TextField priceField = (TextField) dialogPane.lookup("#priceField");
        TextField stockField = (TextField) dialogPane.lookup("#stockField");
        ComboBox<Category> categoryCombo = (ComboBox<Category>) dialogPane.lookup("#categoryCombo");
        CheckBox activeCheckbox = (CheckBox) dialogPane.lookup("#activeCheckbox");
        TextArea descriptionArea = (TextArea) dialogPane.lookup("#descriptionArea");
        Label dialogTitleLabel = (Label) dialogPane.lookup("#dialogTitleLabel");
        Button scanBarcodeButton = (Button) dialogPane.lookup("#scanBarcodeButton");

        // Guardar referencia al campo de código de barras
        barcodeFieldRef = barcodeField;

        // Configurar título
        dialogTitleLabel.setText(title);

        // Cargar categorías
        List<Category> categories = categoryService.getAllActiveCategories();
        categoryCombo.setItems(FXCollections.observableArrayList(categories));

        // Configurar conversión de display para categorías
        categoryCombo.setCellFactory(param -> new ListCell<Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        categoryCombo.setButtonCell(new ListCell<Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        // Establecer los valores actuales si es edición
        if (product.getId() != null) {
            nameField.setText(product.getName());
            skuField.setText(product.getSku());
            barcodeField.setText(product.getBarcode());
            priceField.setText(product.getPrice() != null ? product.getPrice().toString() : "");
            stockField.setText(product.getStock() != null ? product.getStock().toString() : "");
            descriptionArea.setText(product.getDescription());
            activeCheckbox.setSelected(product.isActive());

            // Seleccionar la categoría correcta
            if (product.getCategory() != null) {
                for (Category category : categories) {
                    if (category.getId().equals(product.getCategory().getId())) {
                        categoryCombo.setValue(category);
                        break;
                    }
                }
            }

            // Desactivar SKU y código de barras si es edición
            skuField.setEditable(false);
            barcodeField.setEditable(false);
            scanBarcodeButton.setDisable(true);
        }

        // Configurar evento para escanear código de barras
        scanBarcodeButton.setOnAction(event -> {
            // Configurar el callback para el escáner
            barcodeScanner.setBarcodeCallback(barcode -> {
                Platform.runLater(() -> {
                    barcodeFieldRef.setText(barcode);
                });
            });

            // Mostrar mensaje para indicar que el escáner está activo
            AlertUtil.showInformation("Escáner Activo",
                    "El escáner de códigos de barras está activo",
                    "Escanee un código de barras para capturarlo");
        });

        // Mostrar el diálogo y esperar resultado
        Optional<ButtonType> result = dialog.showAndWait();

        // Procesar resultado si se confirma
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Validar campos obligatorios
                if (nameField.getText().trim().isEmpty() ||
                        skuField.getText().trim().isEmpty() ||
                        barcodeField.getText().trim().isEmpty() ||
                        priceField.getText().trim().isEmpty() ||
                        stockField.getText().trim().isEmpty() ||
                        categoryCombo.getValue() == null) {

                    AlertUtil.showWarning("Datos incompletos",
                            "Todos los campos marcados son obligatorios");
                    return false;
                }

                // Transferir datos del formulario al objeto
                product.setName(nameField.getText().trim());
                product.setSku(skuField.getText().trim());
                product.setBarcode(barcodeField.getText().trim());
                product.setDescription(descriptionArea.getText().trim());
                product.setActive(activeCheckbox.isSelected());
                product.setCategory(categoryCombo.getValue());

                try {
                    product.setPrice(new BigDecimal(priceField.getText().trim()));
                } catch (NumberFormatException e) {
                    AlertUtil.showWarning("Precio inválido",
                            "El precio debe ser un número válido");
                    return false;
                }

                try {
                    product.setStock(Integer.parseInt(stockField.getText().trim()));
                } catch (NumberFormatException e) {
                    AlertUtil.showWarning("Stock inválido",
                            "El stock debe ser un número entero");
                    return false;
                }

                return true;
            } catch (Exception e) {
                log.error("Error al procesar datos del producto", e);
                AlertUtil.showError("Error", "Error al procesar los datos", e.getMessage());
                return false;
            }
        }

        return false;
    }

    /**
     * Confirma y elimina un producto
     */
    private void confirmAndDeleteProduct(Product product) {
        boolean confirmed = AlertUtil.showConfirmation(
                "Confirmar Eliminación",
                "¿Está seguro de eliminar este producto?",
                "Esta acción desactivará el producto: " + product.getName());

        if (confirmed) {
            try {
                // Desactivar producto (eliminación suave)
                productService.deactivateProduct(product.getId());

                // Eliminar de la lista
                productsList.remove(product);
                updateTotalProductsLabel();

                // Mostrar mensaje de éxito
                AlertUtil.showInformation("Producto Eliminado",
                        "Producto eliminado correctamente", null);
            } catch (Exception e) {
                log.error("Error al eliminar producto", e);
                AlertUtil.showError("Error", "Error al eliminar producto", e.getMessage());
            }
        }
    }

    /**
     * Encuentra el índice de un producto en la lista por su ID
     */
    private int findProductIndexInList(Long productId) {
        for (int i = 0; i < productsList.size(); i++) {
            if (productsList.get(i).getId().equals(productId)) {
                return i;
            }
        }
        return -1;
    }
}
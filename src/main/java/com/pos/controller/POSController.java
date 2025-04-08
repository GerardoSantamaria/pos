package com.pos.controller;

import com.pos.models.Product;
import com.pos.models.Sale;
import com.pos.models.SaleItem;
import com.pos.services.AuthenticationService;
import com.pos.services.ProductService;
import com.pos.services.SaleService;
import com.pos.utils.AlertUtil;
import com.pos.services.BarcodeScannerService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

@Component
public class POSController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(POSController.class);

    private final SaleService saleService;
    private final ProductService productService;
    private final BarcodeScannerService barcodeScanner;
    private final ApplicationContext applicationContext;
    private final AuthenticationService authService;

    // Elementos de la UI
    @FXML private TextField barcodeField;
    @FXML private TextField quantityField;
    @FXML private TextField searchField;

    @FXML private TableView<SaleItem> cartTable;
    @FXML private TableColumn<SaleItem, String> productColumn;
    @FXML private TableColumn<SaleItem, Integer> quantityColumn;
    @FXML private TableColumn<SaleItem, String> unitPriceColumn;
    @FXML private TableColumn<SaleItem, String> totalColumn;

    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;

    @FXML private ComboBox<Sale.PaymentMethod> paymentMethodCombo;
    @FXML private TextField customerNameField;
    @FXML private TextField customerTaxIdField;

    @FXML private Button addButton;
    @FXML private Button removeButton;
    @FXML private Button completeButton;
    @FXML private Button cancelButton;
    @FXML private Button newSaleButton;

    // Estado interno
    private Sale currentSale;
    private ObservableList<SaleItem> cartItems = FXCollections.observableArrayList();
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

    // Constructor con inyección de dependencias en lugar de @RequiredArgsConstructor
    public POSController(SaleService saleService, ProductService productService, BarcodeScannerService barcodeScanner, ApplicationContext applicationContext, AuthenticationService authService) {
        this.saleService = saleService;
        this.productService = productService;
        this.barcodeScanner = barcodeScanner;
        this.applicationContext = applicationContext;
        this.authService = authService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar la tabla
        initializeTable();

        // Configurar el combo de métodos de pago
        paymentMethodCombo.getItems().addAll(Sale.PaymentMethod.values());
        paymentMethodCombo.setValue(Sale.PaymentMethod.CASH);

        // Configurar campo de cantidad con valor predeterminado
        quantityField.setText("1");

        // Configurar eventos de botones
        addButton.setOnAction(event -> addProductToCart());
        removeButton.setOnAction(event -> removeSelectedItem());
        completeButton.setOnAction(event -> completeSale());
        cancelButton.setOnAction(event -> cancelSale());
        newSaleButton.setOnAction(event -> startNewSale());

        // Configurar evento de tecla para búsqueda rápida
        barcodeField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                addProductToCart();
            }
        });

        // Configurar el escáner de códigos de barras
        barcodeScanner.setBarcodeCallback(this::handleBarcodeScanned);

        // Iniciar una nueva venta
        startNewSale();
    }

    private void initializeTable() {
        // Configurar las columnas de la tabla
        productColumn.setCellValueFactory(new PropertyValueFactory<>("product"));
        productColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProduct().getName()));

        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        unitPriceColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(currencyFormat.format(cellData.getValue().getUnitPrice())));

        totalColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(currencyFormat.format(cellData.getValue().getTotal())));

        // Establecer la fuente de datos
        cartTable.setItems(cartItems);
    }

    /**
     * Maneja el evento de código de barras escaneado
     */
    private void handleBarcodeScanned(String barcode) {
        // Hay que asegurarse de que estamos en el hilo de JavaFX
        Platform.runLater(() -> {
            barcodeField.setText(barcode);
            addProductToCart();
        });
    }

    /**
     * Inicia una nueva venta
     */
    private void startNewSale() {
        try {
            currentSale = saleService.createNewSale();
            LOGGER.info("Nueva venta iniciada: {}", currentSale.getInvoiceNumber());

            // Limpiar la interfaz
            cartItems.clear();
            updateTotals();

            customerNameField.clear();
            customerTaxIdField.clear();
            barcodeField.clear();
            quantityField.setText("1");

            // Habilitar/deshabilitar botones apropiados
            updateButtonStates(true);

            // Enfocar el campo de código de barras
            barcodeField.requestFocus();
        } catch (Exception e) {
            LOGGER.error("Error al iniciar nueva venta", e);
            AlertUtil.showError("Error", "No se pudo iniciar una nueva venta", e.getMessage());
        }
    }

    /**
     * Añade un producto al carrito usando el código de barras
     */
    private void addProductToCart() {
        if (currentSale.getStatus() != Sale.SaleStatus.IN_PROGRESS) {
            AlertUtil.showWarning("Venta no activa", "No hay una venta activa en este momento");
            return;
        }

        String barcode = barcodeField.getText().trim();
        if (barcode.isEmpty()) {
            AlertUtil.showWarning("Código vacío", "Ingrese o escanee un código de barras");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) {
                throw new NumberFormatException("La cantidad debe ser mayor que cero");
            }
        } catch (NumberFormatException e) {
            AlertUtil.showWarning("Cantidad inválida", "Ingrese un número entero positivo");
            return;
        }

        try {
            SaleItem item = saleService.addProductByBarcode(currentSale.getId(), barcode, quantity);

            // Actualizar la tabla y los totales
            refreshCartItems();
            updateTotals();

            // Limpiar campos
            barcodeField.clear();
            quantityField.setText("1");
            barcodeField.requestFocus();
        } catch (Exception e) {
            LOGGER.error("Error al añadir producto", e);
            AlertUtil.showError("Error", "No se pudo añadir el producto", e.getMessage());
        }
    }

    /**
     * Elimina el ítem seleccionado del carrito
     */
    private void removeSelectedItem() {
        SaleItem selectedItem = cartTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            AlertUtil.showWarning("Selección vacía", "Seleccione un producto para eliminar");
            return;
        }

        try {
            saleService.removeItem(currentSale.getId(), selectedItem.getId());

            // Actualizar la tabla y los totales
            refreshCartItems();
            updateTotals();
        } catch (Exception e) {
            LOGGER.error("Error al eliminar ítem", e);
            AlertUtil.showError("Error", "No se pudo eliminar el ítem", e.getMessage());
        }
    }

    /**
     * Completa la venta actual
     */
    private void completeSale() {
        if (cartItems.isEmpty()) {
            AlertUtil.showWarning("Carrito vacío", "Añada al menos un producto para completar la venta");
            return;
        }

        try {
            Sale.PaymentMethod paymentMethod = paymentMethodCombo.getValue();
            String customerName = customerNameField.getText().trim();
            String customerTaxId = customerTaxIdField.getText().trim();

            Sale completedSale = saleService.completeSale(
                    currentSale.getId(), paymentMethod, customerName, customerTaxId);

            // Mostrar mensaje de éxito
            AlertUtil.showInformation(
                    "Venta completada",
                    "Venta #" + completedSale.getInvoiceNumber() + " completada con éxito",
                    "Total: " + currencyFormat.format(completedSale.getTotalAmount())
            );

            // Desactivar botones de modificación
            updateButtonStates(false);

        } catch (Exception e) {
            LOGGER.error("Error al completar venta", e);
            AlertUtil.showError("Error", "No se pudo completar la venta", e.getMessage());
        }
    }

    /**
     * Cancela la venta actual
     */
    private void cancelSale() {
        if (AlertUtil.showConfirmation(
                "Cancelar venta",
                "¿Está seguro de cancelar esta venta?",
                "Esta acción no se puede deshacer")) {

            try {
                saleService.cancelSale(currentSale.getId());
                AlertUtil.showInformation("Venta cancelada", "La venta ha sido cancelada", null);

                // Desactivar botones de modificación
                updateButtonStates(false);

            } catch (Exception e) {
                LOGGER.error("Error al cancelar venta", e);
                AlertUtil.showError("Error", "No se pudo cancelar la venta", e.getMessage());
            }
        }
    }

    /**
     * Refresca los ítems del carrito desde la base de datos
     */
    private void refreshCartItems() {
        // Recargar la venta desde la base de datos
        try {
            Sale refreshedSale = saleService.getSaleById(currentSale.getId());
            currentSale = refreshedSale;

            // Actualizar los items del carrito
            cartItems.clear();
            cartItems.addAll(currentSale.getItems());
        } catch (Exception e) {
            LOGGER.error("Error al refrescar ítems", e);
        }
    }

    /**
     * Actualiza las etiquetas de totales
     */
    private void updateTotals() {
        subtotalLabel.setText(currencyFormat.format(currentSale.getSubtotal()));
        taxLabel.setText(currencyFormat.format(currentSale.getTaxAmount()));
        totalLabel.setText(currencyFormat.format(currentSale.getTotalAmount()));
    }

    /**
     * Actualiza el estado de los botones según si la venta está activa
     */
    private void updateButtonStates(boolean isActive) {
        addButton.setDisable(!isActive);
        removeButton.setDisable(!isActive);
        completeButton.setDisable(!isActive);
        cancelButton.setDisable(!isActive);
        barcodeField.setDisable(!isActive);
        quantityField.setDisable(!isActive);
        customerNameField.setDisable(!isActive);
        customerTaxIdField.setDisable(!isActive);
        paymentMethodCombo.setDisable(!isActive);
    }

    /**
     * Busca productos por nombre para ayudar en la búsqueda manual
     */
    @FXML
    private void searchProducts() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            return;
        }

        List<Product> products = productService.searchProductsByName(query);

        if (products.isEmpty()) {
            AlertUtil.showInformation("Búsqueda", "No se encontraron productos", "Intente con otro término de búsqueda");
            return;
        }

        // Mostrar los productos en un diálogo para selección
        ChoiceDialog<Product> dialog = new ChoiceDialog<>(products.get(0), products);
        dialog.setTitle("Seleccionar producto");
        dialog.setHeaderText("Productos encontrados");
        dialog.setContentText("Seleccione un producto:");

        Optional<Product> result = dialog.showAndWait();
        result.ifPresent(product -> {
            barcodeField.setText(product.getBarcode());
        });
    }

    /**
     * Maneja el evento de cierre de sesión
     */
    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            // Cerrar la sesión del usuario actual
            authService.logout();
            LOGGER.info("Usuario desconectado");

            // Cargar la pantalla de LOGGERin
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/templates/login.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            // Obtener el stage actual desde el evento
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Configurar la nueva escena
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/static/css/styles.css").toExternalForm());

            // Mostrar la pantalla de LOGGERin
            stage.setScene(scene);
            stage.setTitle("Login - Sistema POS");
            stage.setMaximized(false);
            stage.setWidth(800);
            stage.setHeight(600);
            stage.centerOnScreen();
            stage.show();

            LOGGER.info("Pantalla de Login cargada correctamente");
        } catch (Exception e) {
            LOGGER.error("Error al cerrar sesión", e);
            AlertUtil.showError("Error", "Error al cerrar sesión", e.getMessage());
        }
    }

    /**
     * Abre la pantalla de gestión de productos
     */
    @FXML
    public void openProductsScreen(ActionEvent event) {
        try {
            loadScreen("/templates/products.fxml", "Productos - Sistema POS");
            LOGGER.info("Pantalla de productos cargada correctamente");
        } catch (Exception e) {
            LOGGER.error("Error al cargar pantalla de productos", e);
            AlertUtil.showError("Error", "Error al cargar pantalla de productos", e.getMessage());
        }
    }

    /**
     * Abre la pantalla de inventario
     */
    @FXML
    public void openInventoryScreen(ActionEvent event) {
        try {
            // Esta parte sería implementada cuando se cree la vista de inventario
            //loadScreen("/templates/inventory.fxml", "Inventario - Sistema POS");

            // Por ahora, mostrar un mensaje informativo
            AlertUtil.showInformation("En desarrollo",
                    "Funcionalidad en desarrollo",
                    "La pantalla de inventario está en desarrollo");

            LOGGER.info("Pantalla de inventario solicitada (no implementada)");
        } catch (Exception e) {
            LOGGER.error("Error al intentar cargar pantalla de inventario", e);
            AlertUtil.showError("Error", "Error al cargar pantalla", e.getMessage());
        }
    }

    /**
     * Abre la pantalla de historial de ventas
     */
    @FXML
    public void openSalesScreen(ActionEvent event) {
        try {
            loadScreen("/templates/sales_history.fxml", "Historial de Ventas - Sistema POS");
            LOGGER.info("Pantalla de historial de ventas cargada correctamente");
        } catch (Exception e) {
            LOGGER.error("Error al cargar pantalla de historial de ventas", e);
            AlertUtil.showError("Error", "Error al cargar pantalla de historial", e.getMessage());
        }
    }

    /**
     * Abre la pantalla de reportes
     */
    @FXML
    public void openReportsScreen(ActionEvent event) {
        try {
            // Esta parte sería implementada cuando se cree la vista de reportes
            //loadScreen("/templates/reports.fxml", "Reportes - Sistema POS");

            // Por ahora, mostrar un mensaje informativo
            AlertUtil.showInformation("En desarrollo",
                    "Funcionalidad en desarrollo",
                    "La pantalla de reportes está en desarrollo");

            LOGGER.info("Pantalla de reportes solicitada (no implementada)");
        } catch (Exception e) {
            LOGGER.error("Error al intentar cargar pantalla de reportes", e);
            AlertUtil.showError("Error", "Error al cargar pantalla", e.getMessage());
        }
    }

    /**
     * Abre la pantalla de configuración
     */
    @FXML
    public void openSettingsScreen(ActionEvent event) {
        try {
            // Esta parte sería implementada cuando se cree la vista de configuración
            //loadScreen("/templates/settings.fxml", "Configuración - Sistema POS");

            // Por ahora, mostrar un mensaje informativo
            AlertUtil.showInformation("En desarrollo",
                    "Funcionalidad en desarrollo",
                    "La pantalla de configuración está en desarrollo");

            LOGGER.info("Pantalla de configuración solicitada (no implementada)");
        } catch (Exception e) {
            LOGGER.error("Error al intentar cargar pantalla de configuración", e);
            AlertUtil.showError("Error", "Error al cargar pantalla", e.getMessage());
        }
    }

    /**
     * Método utilitario para cargar pantallas
     */
    private void loadScreen(String fxmlPath, String title) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        loader.setControllerFactory(applicationContext::getBean);
        Parent root = loader.load();

        // Obtener el stage actual
        Stage stage = (Stage) barcodeField.getScene().getWindow();

        // Configurar la nueva escena
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/static/css/styles.css").toExternalForm());

        // Mostrar la nueva pantalla
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
    }

}
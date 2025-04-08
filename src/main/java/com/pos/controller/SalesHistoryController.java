package com.pos.controller;

import com.pos.models.Sale;
import com.pos.models.SaleItem;
import com.pos.models.User;
import com.pos.services.SaleService;
import com.pos.utils.AlertUtil;
import com.pos.utils.FormatterUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;

@Component
public class SalesHistoryController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SalesHistoryController.class);

    private final SaleService saleService;
    private final ApplicationContext applicationContext;

    // Referencias a elementos de UI
    @FXML private TableView<Sale> salesTable;
    @FXML private TableColumn<Sale, String> invoiceColumn;
    @FXML private TableColumn<Sale, String> dateColumn;
    @FXML private TableColumn<Sale, String> customerColumn;
    @FXML private TableColumn<Sale, String> cashierColumn;
    @FXML private TableColumn<Sale, String> paymentMethodColumn;
    @FXML private TableColumn<Sale, String> totalColumn;
    @FXML private TableColumn<Sale, String> statusColumn;
    @FXML private TableColumn<Sale, Void> actionsColumn;

    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private ComboBox<Sale.SaleStatus> statusComboBox;
    @FXML private Label totalSalesLabel;

    // Datos para la tabla
    private final ObservableList<Sale> salesList = FXCollections.observableArrayList();
    private BigDecimal totalAmount = BigDecimal.ZERO;

    public SalesHistoryController(SaleService saleService, ApplicationContext applicationContext) {
        this.saleService = saleService;
        this.applicationContext = applicationContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar tabla
        setupTableColumns();

        // Configurar filtros
        setupFilters();

        // Cargar datos iniciales (ventas del día actual)
        loadTodaySales();
    }

    /**
     * Configura las columnas de la tabla de ventas
     */
    private void setupTableColumns() {
        // Configurar las columnas
        invoiceColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));

        dateColumn.setCellValueFactory(data ->
                new SimpleStringProperty(FormatterUtil.formatDateTime(data.getValue().getSaleDate())));

        customerColumn.setCellValueFactory(data -> {
            Sale sale = data.getValue();
            String customer = sale.getCustomerName();
            return new SimpleStringProperty(customer != null && !customer.isEmpty() ? customer : "Consumidor Final");
        });

        cashierColumn.setCellValueFactory(data -> {
            User cashier = data.getValue().getCashier();
            return new SimpleStringProperty(cashier != null ? cashier.getFullName() : "");
        });

        paymentMethodColumn.setCellValueFactory(data -> {
            Sale.PaymentMethod method = data.getValue().getPaymentMethod();
            if (method == null) {
                return new SimpleStringProperty("");
            }

            // Convertir enumeración a texto amigable
            switch (method) {
                case CASH: return new SimpleStringProperty("Efectivo");
                case CREDIT_CARD: return new SimpleStringProperty("Tarjeta de Crédito");
                case DEBIT_CARD: return new SimpleStringProperty("Tarjeta de Débito");
                case TRANSFER: return new SimpleStringProperty("Transferencia");
                case OTHER: return new SimpleStringProperty("Otro");
                default: return new SimpleStringProperty(method.toString());
            }
        });

        totalColumn.setCellValueFactory(data ->
                new SimpleStringProperty(FormatterUtil.formatCurrency(data.getValue().getTotalAmount())));

        statusColumn.setCellValueFactory(data -> {
            Sale.SaleStatus status = data.getValue().getStatus();
            if (status == null) {
                return new SimpleStringProperty("");
            }

            // Convertir enumeración a texto amigable
            switch (status) {
                case COMPLETED: return new SimpleStringProperty("Completada");
                case IN_PROGRESS: return new SimpleStringProperty("En Progreso");
                case CANCELLED: return new SimpleStringProperty("Cancelada");
                case REFUNDED: return new SimpleStringProperty("Reembolsada");
                default: return new SimpleStringProperty(status.toString());
            }
        });

        // Configurar la columna de acciones
        setupActionsColumn();

        // Establecer los datos
        salesTable.setItems(salesList);
    }

    /**
     * Configura la columna de acciones con botones
     */
    private void setupActionsColumn() {
        Callback<TableColumn<Sale, Void>, TableCell<Sale, Void>> cellFactory =
                new Callback<>() {
                    @Override
                    public TableCell<Sale, Void> call(final TableColumn<Sale, Void> param) {
                        return new TableCell<>() {
                            private final Button viewBtn = new Button("Ver");

                            {
                                viewBtn.getStyleClass().add("action-button");
                                viewBtn.setOnAction(event -> {
                                    Sale sale = getTableView().getItems().get(getIndex());
                                    openSaleDetails(sale);
                                });
                            }

                            private final HBox buttonsBox = new HBox(5, viewBtn);

                            @Override
                            protected void updateItem(Void item, boolean empty) {
                                super.updateItem(item, empty);

                                if (empty) {
                                    setGraphic(null);
                                } else {
                                    Sale sale = getTableView().getItems().get(getIndex());
                                    // Solo mostrar botones si la venta está completada
                                    if (sale.getStatus() == Sale.SaleStatus.COMPLETED) {
                                        setGraphic(buttonsBox);
                                    } else {
                                        setGraphic(null);
                                    }
                                }
                            }
                        };
                    }
                };

        actionsColumn.setCellFactory(cellFactory);
    }

    /**
     * Configura los controles de filtro
     */
    private void setupFilters() {
        // Configurar DatePicker con fecha actual
        LocalDate today = LocalDate.now();
        fromDatePicker.setValue(today);
        toDatePicker.setValue(today);

        // Configurar ComboBox de estados
        statusComboBox.getItems().add(null); // Opción para "Todos"
        statusComboBox.getItems().addAll(Sale.SaleStatus.values());

        // Configurar la conversión de visualización
        statusComboBox.setConverter(new javafx.util.StringConverter<Sale.SaleStatus>() {
            @Override
            public String toString(Sale.SaleStatus status) {
                if (status == null) {
                    return "Todos";
                }
                switch (status) {
                    case COMPLETED: return "Completada";
                    case IN_PROGRESS: return "En Progreso";
                    case CANCELLED: return "Cancelada";
                    case REFUNDED: return "Reembolsada";
                    default: return status.toString();
                }
            }

            @Override
            public Sale.SaleStatus fromString(String string) {
                if (string == null || string.equals("Todos")) {
                    return null;
                }
                switch (string) {
                    case "Completada": return Sale.SaleStatus.COMPLETED;
                    case "En Progreso": return Sale.SaleStatus.IN_PROGRESS;
                    case "Cancelada": return Sale.SaleStatus.CANCELLED;
                    case "Reembolsada": return Sale.SaleStatus.REFUNDED;
                    default: return Sale.SaleStatus.valueOf(string);
                }
            }
        });

        // Seleccionar "Todos" por defecto
        statusComboBox.setValue(null);
    }

    /**
     * Carga las ventas del día actual
     */
    private void loadTodaySales() {
        try {
            List<Sale> todaySales = saleService.getTodaySales();
            salesList.clear();
            salesList.addAll(todaySales);

            // Actualizar total
            calculateTotal();
            updateTotalLabel();

            LOGGER.info("Ventas del día cargadas: {}", salesList.size());
        } catch (Exception e) {
            LOGGER.error("Error al cargar las ventas del día", e);
            AlertUtil.showError("Error", "Error al cargar las ventas del día", e.getMessage());
        }
    }

    /**
     * Abre el diálogo de detalles de una venta
     */
    private void openSaleDetails(Sale sale) {
        try {
            // Cargar el diálogo
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/templates/sale_detail.fxml"));
            DialogPane dialogPane = loader.load();

            // Configurar el diálogo
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Detalle de Venta");

            // Mostrar información de la venta en el diálogo
            populateSaleDetails(dialogPane, sale);

            // Mostrar el diálogo
            dialog.showAndWait();

        } catch (IOException e) {
            LOGGER.error("Error al abrir detalles de venta", e);
            AlertUtil.showError("Error", "Error al abrir detalles de venta", e.getMessage());
        }
    }

    /**
     * Rellena los campos del diálogo de detalles con la información de la venta
     */
    private void populateSaleDetails(DialogPane dialogPane, Sale sale) {
        // Referencias a los elementos UI del diálogo
        Label invoiceNumberLabel = (Label) dialogPane.lookup("#invoiceNumberLabel");
        Label saleDateLabel = (Label) dialogPane.lookup("#saleDateLabel");
        Label customerNameLabel = (Label) dialogPane.lookup("#customerNameLabel");
        Label customerTaxIdLabel = (Label) dialogPane.lookup("#customerTaxIdLabel");
        Label cashierLabel = (Label) dialogPane.lookup("#cashierLabel");
        Label statusLabel = (Label) dialogPane.lookup("#statusLabel");
        Label subtotalLabel = (Label) dialogPane.lookup("#subtotalLabel");
        Label taxLabel = (Label) dialogPane.lookup("#taxLabel");
        Label totalLabel = (Label) dialogPane.lookup("#totalLabel");

        // Tabla de ítems
        TableView<SaleItem> itemsTable = (TableView<SaleItem>) dialogPane.lookup("#itemsTable");

        // Actualizar los labels con la información de la venta
        invoiceNumberLabel.setText(sale.getInvoiceNumber());
        saleDateLabel.setText(FormatterUtil.formatDateTime(sale.getSaleDate()));
        customerNameLabel.setText(sale.getCustomerName() != null ? sale.getCustomerName() : "Consumidor Final");
        customerTaxIdLabel.setText(sale.getCustomerTaxId() != null ? sale.getCustomerTaxId() : "--");
        cashierLabel.setText(sale.getCashier() != null ? sale.getCashier().getFullName() : "--");

        // Estado con formato
        String statusText;
        Sale.SaleStatus status = sale.getStatus();
        switch (status) {
            case COMPLETED: statusText = "Completada"; break;
            case IN_PROGRESS: statusText = "En Progreso"; break;
            case CANCELLED: statusText = "Cancelada"; break;
            case REFUNDED: statusText = "Reembolsada"; break;
            default: statusText = status.toString(); break;
        }
        statusLabel.setText(statusText);

        // Totales
        subtotalLabel.setText(FormatterUtil.formatCurrency(sale.getSubtotal()));
        taxLabel.setText(FormatterUtil.formatCurrency(sale.getTaxAmount()));
        totalLabel.setText(FormatterUtil.formatCurrency(sale.getTotalAmount()));

        // Configurar la tabla de ítems
        if (itemsTable != null) {
            setupItemsTable(itemsTable, sale);
        }
    }

    /**
     * Configura la tabla de ítems para el diálogo de detalles
     */
    private void setupItemsTable(TableView<SaleItem> itemsTable, Sale sale) {
        // Obtener referencias a las columnas
        TableColumn<SaleItem, String> productColumn = (TableColumn<SaleItem, String>) itemsTable.getColumns().get(0);
        TableColumn<SaleItem, Integer> quantityColumn = (TableColumn<SaleItem, Integer>) itemsTable.getColumns().get(1);
        TableColumn<SaleItem, String> unitPriceColumn = (TableColumn<SaleItem, String>) itemsTable.getColumns().get(2);
        TableColumn<SaleItem, String> discountColumn = (TableColumn<SaleItem, String>) itemsTable.getColumns().get(3);
        TableColumn<SaleItem, String> totalColumn = (TableColumn<SaleItem, String>) itemsTable.getColumns().get(4);

        // Configurar las celdas de la tabla
        productColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getProduct().getName()));

        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        unitPriceColumn.setCellValueFactory(data ->
                new SimpleStringProperty(FormatterUtil.formatCurrency(data.getValue().getUnitPrice())));

        discountColumn.setCellValueFactory(data ->
                new SimpleStringProperty(FormatterUtil.formatCurrency(data.getValue().getDiscount())));

        totalColumn.setCellValueFactory(data ->
                new SimpleStringProperty(FormatterUtil.formatCurrency(data.getValue().getTotal())));

        // Cargar los datos
        itemsTable.setItems(FXCollections.observableArrayList(sale.getItems()));
    }

    /**
     * Calcula el total de ventas basado en la lista filtrada
     */
    private void calculateTotal() {
        totalAmount = salesList.stream()
                .filter(sale -> sale.getStatus() == Sale.SaleStatus.COMPLETED)
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Actualiza la etiqueta de totales
     */
    private void updateTotalLabel() {
        totalSalesLabel.setText(FormatterUtil.formatCurrency(totalAmount));
    }

    /**
     * Refresca los datos de ventas
     */
    @FXML
    public void refreshSales() {
        loadTodaySales();
    }

    /**
     * Aplica los filtros seleccionados
     */
    @FXML
    public void applyFilters() {
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();
        Sale.SaleStatus selectedStatus = statusComboBox.getValue();

        if (fromDate == null || toDate == null) {
            AlertUtil.showWarning("Fechas incompletas", "Debe seleccionar ambas fechas");
            return;
        }

        if (fromDate.isAfter(toDate)) {
            AlertUtil.showWarning("Rango inválido", "La fecha inicial debe ser anterior o igual a la fecha final");
            return;
        }

        try {
            // Convertir fechas a LocalDateTime para incluir todo el día
            LocalDateTime startDateTime = fromDate.atStartOfDay();
            LocalDateTime endDateTime = toDate.atTime(LocalTime.MAX);

            // Obtener ventas entre fechas
            List<Sale> filteredSales = saleService.getSalesBetweenDates(startDateTime, endDateTime);

            // Filtrar por estado si es necesario
            if (selectedStatus != null) {
                filteredSales = filteredSales.stream()
                        .filter(sale -> sale.getStatus() == selectedStatus)
                        .toList();
            }

            // Actualizar la tabla
            salesList.clear();
            salesList.addAll(filteredSales);

            // Actualizar total
            calculateTotal();
            updateTotalLabel();

            LOGGER.info("Filtro aplicado. Ventas encontradas: {}", salesList.size());
        } catch (Exception e) {
            LOGGER.error("Error al aplicar filtros", e);
            AlertUtil.showError("Error", "Error al aplicar filtros", e.getMessage());
        }
    }

    /**
     * Limpia los filtros y muestra todas las ventas
     */
    @FXML
    public void clearFilters() {
        // Restaurar valores por defecto
        LocalDate today = LocalDate.now();
        fromDatePicker.setValue(today);
        toDatePicker.setValue(today);
        statusComboBox.setValue(null);

        // Recargar ventas del día
        loadTodaySales();
    }
}
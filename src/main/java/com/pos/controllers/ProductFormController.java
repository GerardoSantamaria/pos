package com.pos.controllers;

import com.pos.models.Product;
import com.pos.services.ProductService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the product form view.
 */
@Controller
public class ProductFormController implements Initializable {

    @FXML
    private TextField barcodeField;

    @FXML
    private TextField nameField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField priceField;

    @FXML
    private TextField stockField;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Label titleLabel;

    @FXML
    private Label errorLabel;

    private final ProductService productService;
    private Product product;
    private ProductListController productListController;

    /**
     * Constructor with dependencies.
     *
     * @param productService The product service
     */
    public ProductFormController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Initializes the controller.
     *
     * @param location The location used to resolve relative paths
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Clear error label
        errorLabel.setText("");

        // Set up numeric validation for price field
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                priceField.setText(oldValue);
            }
        });

        // Set up numeric validation for stock field
        stockField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                stockField.setText(oldValue);
            }
        });

        // Set button actions
        saveButton.setOnAction(event -> saveProduct());
        cancelButton.setOnAction(event -> closeForm());
    }

    /**
     * Sets the product to edit. If null, a new product will be created.
     *
     * @param product The product to edit
     */
    public void setProduct(Product product) {
        this.product = product;

        if (product != null) {
            // Editing existing product
            titleLabel.setText("Editar Producto");

            // Populate fields
            barcodeField.setText(product.getBarcode());
            nameField.setText(product.getName());
            descriptionArea.setText(product.getDescription());
            priceField.setText(product.getPrice().toString());
            stockField.setText(product.getStock().toString());
        } else {
            // Creating new product
            titleLabel.setText("Agregar Producto");

            // Clear fields
            barcodeField.clear();
            nameField.clear();
            descriptionArea.clear();
            priceField.clear();
            stockField.clear();
        }
    }

    /**
     * Sets the product list controller for refreshing the list after save.
     *
     * @param productListController The product list controller
     */
    public void setProductListController(ProductListController productListController) {
        this.productListController = productListController;
    }

    /**
     * Saves the product.
     */
    private void saveProduct() {
        // Validate input
        if (!validateInput()) {
            return;
        }

        try {
            // Create or update product
            if (product == null) {
                // Creating new product
                Product newProduct = new Product();
                setProductFields(newProduct);
                productService.createProduct(newProduct);
            } else {
                // Updating existing product
                setProductFields(product);
                productService.updateProduct(product);
            }

            // Refresh product list
            if (productListController != null) {
                productListController.loadProducts();
            }

            // Close form
            closeForm();

        } catch (IllegalArgumentException e) {
            // Show validation error
            errorLabel.setText(e.getMessage());
        } catch (Exception e) {
            // Show general error
            errorLabel.setText("Error al guardar el producto: " + e.getMessage());
        }
    }

    /**
     * Sets the product fields from the form inputs.
     *
     * @param product The product to update
     */
    private void setProductFields(Product product) {
        product.setBarcode(barcodeField.getText().trim());
        product.setName(nameField.getText().trim());
        product.setDescription(descriptionArea.getText().trim());
        product.setPrice(new BigDecimal(priceField.getText().trim()));
        product.setStock(Integer.parseInt(stockField.getText().trim()));
    }

    /**
     * Validates the form input.
     *
     * @return True if input is valid, false otherwise
     */
    private boolean validateInput() {
        // Clear error
        errorLabel.setText("");

        // Validate barcode
        if (barcodeField.getText().trim().isEmpty()) {
            errorLabel.setText("El código de barras es obligatorio");
            barcodeField.requestFocus();
            return false;
        }

        // Validate name
        if (nameField.getText().trim().isEmpty()) {
            errorLabel.setText("El nombre del producto es obligatorio");
            nameField.requestFocus();
            return false;
        }

        // Validate price
        try {
            BigDecimal price = new BigDecimal(priceField.getText().trim());
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                errorLabel.setText("El precio debe ser mayor que cero");
                priceField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("El precio debe ser un número válido");
            priceField.requestFocus();
            return false;
        }

        // Validate stock
        try {
            int stock = Integer.parseInt(stockField.getText().trim());
            if (stock < 0) {
                errorLabel.setText("El stock no puede ser negativo");
                stockField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("El stock debe ser un número entero válido");
            stockField.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Closes the form.
     */
    private void closeForm() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
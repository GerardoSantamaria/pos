package com.pos.controllers.products;

import com.pos.services.core.ModalViewService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@Controller
public class InventoryDashboard implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryDashboard.class);

    private final ApplicationContext applicationContext;
    private final ModalViewService modalViewService;

    @FXML
    private TabPane inventoryTabPane;

    public InventoryDashboard(ApplicationContext applicationContext,
                              ModalViewService modalViewService) {
        this.applicationContext = applicationContext;
        this.modalViewService = modalViewService;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Añadir listener para cargar el contenido solo cuando se selecciona una pestaña
        this.inventoryTabPane.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldTab, newTab) -> {
                    if (newTab != null && newTab.getContent() == null) {
                        loadTabContent(newTab);
                    }
                });

        // Cargar la primera pestaña por defecto
        if (!this.inventoryTabPane.getTabs().isEmpty()) {
            loadTabContent(this.inventoryTabPane.getTabs().getFirst());
        }
    }

    private void loadTabContent(Tab tab) {
        try {
            String tabId = tab.getId();
            InventoryView inventoryView = InventoryView.getByTabId(tabId);
            FXMLLoader loader = this.modalViewService.createFxmlLoaderByPathView(inventoryView.getPathView());
            tab.setContent(loader.load());
        } catch (IOException e) {
            LOGGER.error("Error al cargar tab de Inventario", e);
        }
    }
}

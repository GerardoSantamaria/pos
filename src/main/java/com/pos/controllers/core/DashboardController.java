package com.pos.controllers.core;

import com.pos.config.ViewConfiguration;
import com.pos.manager.core.StageManager;
import com.pos.services.core.AuthService;
import com.pos.services.products.ProductService;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador para la vista principal del dashboard.
 */
@Controller
public class DashboardController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardController.class);

    @FXML
    private BorderPane rootPane;

    @FXML
    private VBox sideMenu;

    @FXML
    private StackPane contentArea;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Button inventoryButton;

    @FXML
    private Button reportsButton;

    @FXML
    private Button configButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Label statusLabel;

    private final AuthService authService;
    private final StageManager stageManager;
    private final ProductService productService;
    private final ApplicationContext applicationContext;

    @Value("${app.ui.animation-speed:300}")
    private int animationSpeed;

    /**
     * Constructor con dependencias.
     *
     * @param authService Servicio de autenticación
     * @param stageManager Gestor de ventanas
     * @param productService Servicio de productos
     * @param applicationContext Contexto de la aplicación Spring
     */
    public DashboardController(
            AuthService authService,
            StageManager stageManager,
            ProductService productService,
            ApplicationContext applicationContext) {
        this.authService = authService;
        this.stageManager = stageManager;
        this.productService = productService;
        this.applicationContext = applicationContext;
    }

    /**
     * Inicializa el controlador.
     *
     * @param location La ubicación para resolver rutas relativas
     * @param resources Los recursos para localizar el objeto raíz
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Aplicar animación de entrada al panel raíz
        LOGGER.info("Inicializando");
        FadeTransition fadeIn = new FadeTransition(Duration.millis(animationSpeed), rootPane);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        // Establecer información del usuario
        setUserInfo();

        // Configurar visibilidad de botones según el rol
        configureMenuAccess();

        // Establecer estado inicial
        updateStatusLabel("Listo");

        // Configurar manejadores de eventos
        setupEventHandlers();

        // Mostrar pantalla de bienvenida
        showWelcomeScreen();
    }

    /**
     * Configura los manejadores de eventos para los botones.
     */
    private void setupEventHandlers() {
        inventoryButton.setOnAction(event -> openInventory());

        // El botón de reportes será funcional en futuras extensiones
        reportsButton.setOnAction(event ->
                showModuleInDevelopment("Módulo de Reportes",
                        "El módulo de reportes estará disponible en futuras versiones."));

        // El botón de configuración será funcional en futuras extensiones
        configButton.setOnAction(event ->
                showModuleInDevelopment("Módulo de Configuración",
                        "El módulo de configuración estará disponible en futuras versiones."));

        logoutButton.setOnAction(event -> logout());
    }

    /**
     * Establece la información del usuario en la interfaz.
     */
    private void setUserInfo() {
        UserDetails currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            usernameLabel.setText(currentUser.getUsername());

            // Establecer etiqueta de rol
            if (authService.hasRole("ROLE_ADMIN")) {
                roleLabel.setText("Administrador");
            } else if (authService.hasRole("ROLE_VENDEDOR")) {
                roleLabel.setText("Vendedor");
            } else {
                roleLabel.setText("Usuario");
            }
        }
    }

    /**
     * Configura el acceso al menú según el rol del usuario.
     */
    private void configureMenuAccess() {
        // El administrador puede acceder a todas las funciones
        if (authService.hasRole("ROLE_ADMIN")) {
            configButton.setDisable(false);
        } else {
            // El vendedor tiene acceso limitado
            configButton.setDisable(true);
        }

        // Reportes disponible para ambos roles (para futura implementación)
        reportsButton.setDisable(false);
    }

    /**
     * Abre la vista de gestión de inventario dentro del área de contenido del dashboard.
     */
    private void openInventory() {
        try {
            // Cargar la vista de inventario en el área de contenido en lugar de cambiar de escena
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(ViewConfiguration.INVENTORY_DASHBOARD));
            fxmlLoader.setControllerFactory(applicationContext::getBean);

            // Cargar el contenido
            Parent inventoryView = fxmlLoader.load();

            // Limpiar contenido actual y establecer el nuevo contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(inventoryView);

            // Actualizar estado
            updateStatusLabel("Módulo de Inventario cargado");
        } catch (Exception e) {
            // Usar Platform.runLater para evitar problemas de threading con diálogos JavaFX
            Platform.runLater(() -> {
                stageManager.showErrorDialog("Error", "Error al cargar el módulo de inventario: " + e.getMessage());
            });
            // Registrar el error
            System.err.println("Error al cargar el módulo de inventario: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Muestra un mensaje informativo para módulos en desarrollo.
     *
     * @param title El título del mensaje
     * @param message El contenido del mensaje
     */
    private void showModuleInDevelopment(String title, String message) {
        // Usar Platform.runLater para evitar problemas de threading
        Platform.runLater(() -> {
            stageManager.showInfoDialog(title, message);
        });

        // También actualizar el área de contenido con un mensaje
        try {
            // Crear un contenedor para el mensaje
            VBox messageContainer = new VBox();
            messageContainer.setAlignment(Pos.CENTER);
            messageContainer.setSpacing(20);
            messageContainer.setPadding(new Insets(50));

            // Título
            Label titleLabel = new Label(title);
            titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

            // Mensaje
            Label messageLabel = new Label(message);
            messageLabel.setStyle("-fx-font-size: 16px;");
            messageLabel.setWrapText(true);

            // Imagen o icono (opcional)
            Label iconLabel = new Label("🚧");
            iconLabel.setStyle("-fx-font-size: 48px;");

            // Añadir componentes al contenedor
            messageContainer.getChildren().addAll(iconLabel, titleLabel, messageLabel);

            // Limpiar y establecer nuevo contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(messageContainer);

            // Actualizar etiqueta de estado
            updateStatusLabel("Módulo en desarrollo");
        } catch (Exception e) {
            System.err.println("Error al mostrar mensaje de módulo en desarrollo: " + e.getMessage());
        }
    }

    /**
     * Muestra una pantalla de bienvenida en el área de contenido.
     */
    private void showWelcomeScreen() {
        try {
            // Crear contenedor para la pantalla de bienvenida
            VBox welcomeContainer = new VBox();
            welcomeContainer.setAlignment(Pos.CENTER);
            welcomeContainer.setSpacing(20);
            welcomeContainer.setPadding(new Insets(50));

            // Título de bienvenida
            Label welcomeTitle = new Label("Bienvenido al Sistema POS");
            welcomeTitle.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");

            // Nombre de usuario
            Label userLabel = new Label("Usuario: " + authService.getCurrentUser().getUsername());
            userLabel.setStyle("-fx-font-size: 18px;");

            // Mensaje de bienvenida
            Label welcomeMessage = new Label(
                    "Por favor, seleccione una opción del menú lateral para comenzar.\n" +
                            "Este sistema le permite gestionar su inventario y realizar ventas de manera eficiente."
            );
            welcomeMessage.setStyle("-fx-font-size: 16px; -fx-text-alignment: center;");
            welcomeMessage.setWrapText(true);
            welcomeMessage.setMaxWidth(500);

            // Añadir componentes al contenedor
            welcomeContainer.getChildren().addAll(welcomeTitle, userLabel, welcomeMessage);

            // Limpiar y establecer nuevo contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(welcomeContainer);

            // Actualizar etiqueta de estado
            updateStatusLabel("Listo");
        } catch (Exception e) {
            System.err.println("Error al mostrar pantalla de bienvenida: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Actualiza la etiqueta de estado.
     *
     * @param status El mensaje de estado
     */
    public void updateStatusLabel(String status) {
        statusLabel.setText(status);
    }

    /**
     * Cierra la sesión del usuario actual y regresa a la pantalla de login.
     */
    private void logout() {
        // Confirmar cierre de sesión usando Platform.runLater para evitar problemas de threading
        Platform.runLater(() -> {
            boolean confirmed = stageManager.showConfirmationDialog(
                    "Cerrar sesión",
                    "¿Está seguro que desea cerrar sesión?");

            if (confirmed) {
                // Cerrar sesión del usuario
                authService.logout();

                // Transición a pantalla de login
                FadeTransition fadeOut = new FadeTransition(Duration.millis(animationSpeed), rootPane);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> stageManager.switchScene(ViewConfiguration.LOGIN_VIEW));
                fadeOut.play();
            }
        });
    }
}
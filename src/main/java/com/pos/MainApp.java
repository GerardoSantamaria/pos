package com.pos;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class MainApp extends Application {

	private ConfigurableApplicationContext springContext;
	private Parent rootNode;
	private FXMLLoader fxmlLoader;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void init() throws Exception {
		// Iniciar el contexto de Spring Boot
		springContext = SpringApplication.run(MainApp.class);

		// Preparar el cargador de FXML con el contexto de Spring
		fxmlLoader = new FXMLLoader(getClass().getResource("/templates/login.fxml"));
		fxmlLoader.setControllerFactory(springContext::getBean);

		// Cargar la vista
		rootNode = fxmlLoader.load();
	}

	@Override
	public void start(Stage primaryStage) {
		// Configurar la escena y el escenario
		primaryStage.setTitle("Sistema POS");

		Scene scene = new Scene(rootNode, 1024, 768);
		scene.getStylesheets().add(getClass().getResource("/static/css/styles.css").toExternalForm());

		primaryStage.setScene(scene);
		primaryStage.setMinWidth(800);
		primaryStage.setMinHeight(600);
		primaryStage.show();
	}

	@Override
	public void stop() {
		// Cerrar el contexto de Spring al salir
		springContext.close();
		Platform.exit();
	}
}
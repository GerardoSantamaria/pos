package com.pos;

import com.pos.manager.core.CssManager;
import com.pos.manager.core.StageManager;
import com.pos.config.ViewConfiguration;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Main application class that serves as the entry point for the POS system.
 * Integrates Spring Boot with JavaFX.
 */
@SpringBootApplication
public class MainApp extends Application {

	private ConfigurableApplicationContext springContext;

	/**
	 * Main method that launches the JavaFX application.
	 *
	 * @param args Command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}

	/**
	 * Initializes Spring context before JavaFX application starts.
	 *
	 * @throws Exception If initialization fails
	 */
	@Override
	public void init() throws Exception {
		springContext = SpringApplication.run(MainApp.class);
	}

	/**
	 * Starts the JavaFX application, setting up the primary stage and showing the login screen.
	 *
	 * @param primaryStage The primary stage for the application
	 */
	@Override
	public void start(Stage primaryStage) {
        StageManager stageManager = springContext.getBean(StageManager.class);
		stageManager.setPrimaryStage(primaryStage);

		CssManager cssManager = springContext.getBean(CssManager.class);
		cssManager.setLightTheme();

		// Set application title
		primaryStage.setTitle("POS System");
		// Display login screen
		stageManager.switchScene(ViewConfiguration.LOGIN_VIEW);
	}

	/**
	 * Cleans up resources when the application is stopped.
	 *
	 * @throws Exception If cleanup fails
	 */
	@Override
	public void stop() throws Exception {
		springContext.close();
		Platform.exit();
	}
}
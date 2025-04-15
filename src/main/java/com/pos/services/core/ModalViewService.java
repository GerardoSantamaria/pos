package com.pos.services.core;

import com.pos.config.ViewConfiguration;
import com.pos.interfaces.core.FormInitialize;
import com.pos.manager.core.StageManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ModalViewService {

    private final StageManager stageManager;
    private final AuthService authService;
    private final ApplicationContext applicationContext;


    public ModalViewService(StageManager stageManager,
                            AuthService authService,
                            ApplicationContext applicationContext) {
        this.stageManager = stageManager;
        this.authService = authService;
        this.applicationContext = applicationContext;
    }

    public FXMLLoader createFxmlLoaderByPathView(String path) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(ViewConfiguration.PRODUCT_FORM_VIEW));
        fxmlLoader.setControllerFactory(applicationContext::getBean);
        return fxmlLoader;
    }

    public <T> void loadViewByPathAndParam(String path, String title, T param) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(path));
        fxmlLoader.setControllerFactory(applicationContext::getBean);
        Parent formRoot = fxmlLoader.load();

        // Get the controller and set the parameter
        FormInitialize controller = fxmlLoader.getController();
        controller.formInitialize(param);

        // Create a new stage for the form
        Stage formStage = new Stage();
        formStage.initModality(Modality.APPLICATION_MODAL);
        formStage.initOwner(stageManager.getPrimaryStage());
        formStage.setTitle(title);
        formStage.setScene(new Scene(formRoot));
        formStage.showAndWait();
    }
}

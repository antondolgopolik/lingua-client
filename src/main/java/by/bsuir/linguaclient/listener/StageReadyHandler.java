package by.bsuir.linguaclient.listener;

import by.bsuir.linguaclient.api.lingua.LinguaClient;
import by.bsuir.linguaclient.controller.CatalogController;
import by.bsuir.linguaclient.controller.LoginController;
import by.bsuir.linguaclient.event.StageReadyEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class StageReadyHandler implements ApplicationListener<StageReadyEvent> {

    private final FxWeaver fxWeaver;
    private final LinguaClient linguaClient;
    private final String applicationTitle;

    @Autowired
    public StageReadyHandler(FxWeaver fxWeaver,
                             LinguaClient linguaClient,
                             @Value("${app.title}") String applicationTitle) {
        this.fxWeaver = fxWeaver;
        this.linguaClient = linguaClient;
        this.applicationTitle = applicationTitle;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        Stage stage = event.getStage();
        Parent root = linguaClient.isLoggedIn()
                ? fxWeaver.loadView(CatalogController.class)
                : fxWeaver.loadView(LoginController.class);
        Scene scene = new Scene(root);
        stage.setScene(scene);

        stage.setTitle(applicationTitle);
        stage.setMaximized(true);
        stage.show();
    }
}
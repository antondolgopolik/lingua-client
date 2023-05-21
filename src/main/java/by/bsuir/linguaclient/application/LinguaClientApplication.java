package by.bsuir.linguaclient.application;

import by.bsuir.linguaclient.ApplicationLauncher;
import by.bsuir.linguaclient.event.StageReadyEvent;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class LinguaClientApplication extends Application {

    private static LinguaClientApplication instance;
    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        instance = this;
        applicationContext = new SpringApplicationBuilder()
                .sources(ApplicationLauncher.class)
                .run();
    }

    @Override
    public void start(Stage stage) {
        applicationContext.publishEvent(new StageReadyEvent(stage));
    }

    @Override
    public void stop() {
        applicationContext.close();
        Platform.exit();
    }

    public static LinguaClientApplication getInstance() {
        return instance;
    }
}

package by.bsuir.linguaclient;

import by.bsuir.linguaclient.application.LinguaClientApplication;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApplicationLauncher {

    public static void main(String[] args) {
        Application.launch(LinguaClientApplication.class, args);
    }
}

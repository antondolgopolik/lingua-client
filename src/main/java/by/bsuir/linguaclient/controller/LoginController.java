package by.bsuir.linguaclient.controller;

import by.bsuir.linguaclient.api.lingua.LinguaClient;
import by.bsuir.linguaclient.dto.lingua.LoginFormDto;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

@Component
@FxmlView("/fxml/LoginView.fxml")
@Slf4j
public class LoginController implements Initializable {

    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button goToRegistrationButton;
    @FXML
    private Button logInButton;

    private final FxWeaver fxWeaver;
    private final LinguaClient linguaClient;

    public LoginController(FxWeaver fxWeaver,
                           LinguaClient linguaClient) {
        this.fxWeaver = fxWeaver;
        this.linguaClient = linguaClient;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        goToRegistrationButton.setOnAction(this::goToRegistrationButtonActionHandler);
        logInButton.setOnAction(this::logInButtonActionHandler);
    }

    private void goToRegistrationButtonActionHandler(ActionEvent event) {
        goToRegistrationButton.getScene().setRoot(fxWeaver.loadView(RegisterController.class));
    }

    private void logInButtonActionHandler(ActionEvent event) {
        String username = usernameTextField.getText();
        String password = passwordField.getText();
        LoginFormDto loginFormDto = new LoginFormDto();
        loginFormDto.setUsername(username);
        loginFormDto.setPassword(password);
        try {
            boolean loggedIn = linguaClient.logIn(loginFormDto).get();
            if (loggedIn) {
                log.info("logged in");
                logInButton.getScene().setRoot(fxWeaver.loadView(CatalogController.class));
            } else {
                log.info("failed to log in");
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}

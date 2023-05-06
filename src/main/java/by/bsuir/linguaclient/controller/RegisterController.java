package by.bsuir.linguaclient.controller;

import by.bsuir.linguaclient.api.lingua.LinguaClient;
import by.bsuir.linguaclient.dto.lingua.RegistrationFormDto;
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
import java.util.function.Consumer;

@Component
@FxmlView("/fxml/RegisterView.fxml")
@Slf4j
public class RegisterController implements Initializable {

    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField repeatPasswordField;
    @FXML
    private TextField tgCodeTextField;
    @FXML
    private Button backToLoginButton;
    @FXML
    private Button registerButton;

    private final FxWeaver fxWeaver;
    private final LinguaClient linguaClient;


    public RegisterController(FxWeaver fxWeaver,
                              LinguaClient linguaClient) {
        this.fxWeaver = fxWeaver;
        this.linguaClient = linguaClient;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        backToLoginButton.setOnAction(this::backToLoginButtonActionHandler);
        registerButton.setOnAction(this::registerButtonActionHandler);
    }

    private void backToLoginButtonActionHandler(ActionEvent event) {
        backToLoginButton.getScene().setRoot(fxWeaver.loadView(LoginController.class));
    }

    private void registerButtonActionHandler(ActionEvent event) {
        String username = usernameTextField.getText();
        String password = passwordField.getText();
        String repeatPasswordField = this.repeatPasswordField.getText();
        String tgCode = tgCodeTextField.getText();
        RegistrationFormDto registrationFormDto = new RegistrationFormDto();
        registrationFormDto.setUsername(username);
        registrationFormDto.setPassword(password);
        registrationFormDto.setTgCode(tgCode);
        try {
            boolean registered = linguaClient.register(registrationFormDto).get();
            if (registered) {
                log.info("registered");
                registerButton.getScene().setRoot(fxWeaver.loadView(CatalogController.class));
            } else {
                log.info("failed to register");
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}

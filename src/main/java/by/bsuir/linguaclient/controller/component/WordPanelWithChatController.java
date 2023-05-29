package by.bsuir.linguaclient.controller.component;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

@Component
@Scope("prototype")
@FxmlView("/fxml/WordPanelWithChatView.fxml")
@Slf4j
public class WordPanelWithChatController extends AbstractWordPanelController implements Initializable {

    @FXML
    private VBox chatVBox;
    @FXML
    private VBox messagesVBox;
    @FXML
    private TextArea messageTextArea;
    @FXML
    private Button sendButton;

    private String username;
    private Consumer<String> messageSendHandler;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        sendButton.setOnAction(event -> {
            String message = messageTextArea.getText();
            messageTextArea.clear();
            messageSendHandler.accept(message);
            showMessage(username, message, true);
        });
    }

    public void fill(String username, Consumer<String> messageSendHandler) {
        this.username = username;
        this.messageSendHandler = messageSendHandler;
    }

    public void showMessage(String username, String message, boolean owner) {
        Text nameText = new Text(username);
        nameText.getStyleClass().add(owner ? "owner-username-text" : "non-owner-username-text");
        Text messageText = new Text(": " + message);
        messageText.getStyleClass().add("message-text");
        TextFlow textFlow = new TextFlow(nameText, messageText);
        messagesVBox.getChildren().add(textFlow);
    }
}

package by.bsuir.linguaclient.controller.component;

import by.bsuir.linguaclient.api.lingua.LinguaClient;
import by.bsuir.linguaclient.dto.lingua.PersonalDuoWatchRequestDto;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import net.rgielen.fxweaver.core.FxWeaver;

public abstract class AbstractPersonalDuoWatchRequestItemController {

    @FXML
    protected PersonalDuoWatchRequestItemInfoController personalDuoWatchRequestItemInfoController;
    @FXML
    protected VBox actionVBox;

    protected final FxWeaver fxWeaver;
    protected final LinguaClient linguaClient;

    protected AbstractPersonalDuoWatchRequestItemController(FxWeaver fxWeaver, LinguaClient linguaClient) {
        this.fxWeaver = fxWeaver;
        this.linguaClient = linguaClient;
    }

    public void fill(PersonalDuoWatchRequestDto personalDuoWatchRequestDto, boolean owner) {
        personalDuoWatchRequestItemInfoController.fill(personalDuoWatchRequestDto);
    }

    protected void addActionButton(String text, EventHandler<ActionEvent> eventHandler) {
        Button button = new Button(text);
        button.setPrefWidth(250);
        button.setStyle("-fx-font-size: 18");
        button.setOnAction(eventHandler);
        actionVBox.getChildren().add(button);
    }
}

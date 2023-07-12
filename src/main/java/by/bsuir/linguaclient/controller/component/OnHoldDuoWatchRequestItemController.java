package by.bsuir.linguaclient.controller.component;

import by.bsuir.linguaclient.api.lingua.LinguaClient;
import by.bsuir.linguaclient.dto.lingua.PersonalDuoWatchRequestDto;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

@Component
@Scope("prototype")
@FxmlView("/fxml/OnHoldDuoWatchRequestItemView.fxml")
@Slf4j
public class OnHoldDuoWatchRequestItemController extends AbstractPersonalDuoWatchRequestItemController {

    public OnHoldDuoWatchRequestItemController(FxWeaver fxWeaver, LinguaClient linguaClient) {
        super(fxWeaver, linguaClient);
    }

    @Override
    public void fill(PersonalDuoWatchRequestDto personalDuoWatchRequestDto, boolean owner) {
        super.fill(personalDuoWatchRequestDto, owner);
        if (owner) {
            addActionButton("Accept", event -> {
                actionVBox.setDisable(true);
                try {
                    boolean success = linguaClient.acceptDuoWatchRequest(personalDuoWatchRequestDto.getId()).get();
                    Alert alert;
                    if (success) {
                        alert = new Alert(Alert.AlertType.INFORMATION, "Duo Watch Response was successfully accepted", ButtonType.OK);
                    } else {
                        alert = new Alert(Alert.AlertType.WARNING, "Duo Watch Response wasn't accepted", ButtonType.OK);
                    }
                    alert.getDialogPane().getStylesheets().add(getClass().getResource("/css/AddWordDialogViewStyle.css").toExternalForm());
                    alert.showAndWait();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });
            addActionButton("Reject", new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    actionVBox.setDisable(true);
                }
            });
            addActionButton("Delete", new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    actionVBox.setDisable(true);
                }
            });
        } else {
            addActionButton("Cancel", new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    actionVBox.setDisable(true);
                }
            });
        }
    }
}

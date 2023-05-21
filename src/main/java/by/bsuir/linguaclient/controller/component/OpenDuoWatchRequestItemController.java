package by.bsuir.linguaclient.controller.component;

import by.bsuir.linguaclient.api.lingua.LinguaClient;
import by.bsuir.linguaclient.dto.lingua.PersonalDuoWatchRequestDto;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
@Scope("prototype")
@FxmlView("/fxml/OpenDuoWatchRequestItemView.fxml")
@Slf4j
public class OpenDuoWatchRequestItemController extends AbstractPersonalDuoWatchRequestItemController implements Initializable {

    @FXML
    private Button confirmRelevanceButton;
    @FXML
    private Button deleteButton;

    public OpenDuoWatchRequestItemController(FxWeaver fxWeaver, LinguaClient linguaClient) {
        super(fxWeaver, linguaClient);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @Override
    public void fill(PersonalDuoWatchRequestDto personalDuoWatchRequestDto, boolean owner) {
        super.fill(personalDuoWatchRequestDto, owner);
        if (!personalDuoWatchRequestDto.getRelevanceConfirmationRequired()) {
            confirmRelevanceButton.setVisible(false);
        }
    }
}

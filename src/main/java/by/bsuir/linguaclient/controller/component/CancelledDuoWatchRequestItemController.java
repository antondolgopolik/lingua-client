package by.bsuir.linguaclient.controller.component;

import by.bsuir.linguaclient.api.lingua.LinguaClient;
import by.bsuir.linguaclient.dto.lingua.PersonalDuoWatchRequestDto;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@FxmlView("/fxml/CancelledDuoWatchRequestItemView.fxml")
@Slf4j
public class CancelledDuoWatchRequestItemController extends AbstractPersonalDuoWatchRequestItemController {

    public CancelledDuoWatchRequestItemController(FxWeaver fxWeaver, LinguaClient linguaClient) {
        super(fxWeaver, linguaClient);
    }

    @Override
    public void fill(PersonalDuoWatchRequestDto personalDuoWatchRequestDto, boolean owner) {
        super.fill(personalDuoWatchRequestDto, owner);
        addActionButton("Delete", new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

            }
        });
        addActionButton("Open", new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

            }
        });
    }
}

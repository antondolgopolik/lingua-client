package by.bsuir.linguaclient.controller.component;

import by.bsuir.linguaclient.api.lingua.LinguaClient;
import by.bsuir.linguaclient.controller.PlayerController;
import by.bsuir.linguaclient.dto.lingua.LanguageDto;
import by.bsuir.linguaclient.dto.lingua.PersonalDuoWatchRequestDto;
import by.bsuir.linguaclient.dto.lingua.SubtitleDto;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxControllerAndView;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Component
@Scope("prototype")
@FxmlView("/fxml/ClosedDuoWatchRequestItemView.fxml")
@Slf4j
public class ClosedDuoWatchRequestItemController extends AbstractPersonalDuoWatchRequestItemController {

    public ClosedDuoWatchRequestItemController(FxWeaver fxWeaver, LinguaClient linguaClient) {
        super(fxWeaver, linguaClient);
    }

    @Override
    public void fill(PersonalDuoWatchRequestDto personalDuoWatchRequestDto, boolean owner) {
        super.fill(personalDuoWatchRequestDto, owner);
        addActionButton("Watch", event -> {
            var videoContentLocDto = personalDuoWatchRequestDto.getVideoContentLocDto();
            var videoContentLocDtoId = videoContentLocDto.getId();
            var secondLanguage = personalDuoWatchRequestDto.getSecondLanguage();

            try {
                boolean success = linguaClient.createViewHistoryRecord(videoContentLocDto.getId()).get();
                if (!success) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "View history record wasn't created", ButtonType.OK);
                    alert.showAndWait();
                    return;
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }

            FxControllerAndView<PlayerController, Parent> controllerAndView = fxWeaver.load(PlayerController.class);
            PlayerController playerController = controllerAndView.getController();
            try {
                SubtitleDto subtitleDto = linguaClient.getSubtitleDto(videoContentLocDtoId, secondLanguage.getId()).get();
                playerController.fill(
                        personalDuoWatchRequestDto,
                        videoContentLocDtoId,
                        subtitleDto.getId(),
                        videoContentLocDto.getLanguage(),
                        secondLanguage,
                        actionVBox.getScene()
                );
                actionVBox.getScene().setRoot(controllerAndView.getView().orElseThrow());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
        if (owner) {
            addActionButton("Open", new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {

                }
            });
            addActionButton("Delete", new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {

                }
            });
        } else {
            addActionButton("Cancel", new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {

                }
            });
        }
    }
}

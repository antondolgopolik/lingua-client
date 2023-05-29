package by.bsuir.linguaclient.controller.component;

import by.bsuir.linguaclient.dto.lingua.PersonalDuoWatchRequestDto;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxControllerAndView;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
@FxmlView("/fxml/PersonalDuoWatchRequestItemPageView.fxml")
@Slf4j
public class PersonalDuoWatchRequestItemPageController {

    @FXML
    private VBox itemVBox;

    private final FxWeaver fxWeaver;

    public PersonalDuoWatchRequestItemPageController(FxWeaver fxWeaver) {
        this.fxWeaver = fxWeaver;
    }

    public void fill(List<PersonalDuoWatchRequestDto> personalDuoWatchRequestDtos, boolean owner) {
        List<Node> items = new ArrayList<>();
        for (PersonalDuoWatchRequestDto personalDuoWatchRequestDto : personalDuoWatchRequestDtos) {
            Class<? extends AbstractPersonalDuoWatchRequestItemController> clazz;
            switch (personalDuoWatchRequestDto.getStatus()) {
                case OPEN -> clazz = OpenDuoWatchRequestItemController.class;
                case ON_HOLD -> clazz = OnHoldDuoWatchRequestItemController.class;
                case CLOSED -> clazz = ClosedDuoWatchRequestItemController.class;
                default -> clazz = CancelledDuoWatchRequestItemController.class;
            }
            FxControllerAndView<? extends AbstractPersonalDuoWatchRequestItemController, HBox> controllerAndView = fxWeaver.load(clazz);
            controllerAndView.getController().fill(personalDuoWatchRequestDto, owner);
            items.add(controllerAndView.getView().orElseThrow());
        }
        itemVBox.getChildren().addAll(items);
    }
}

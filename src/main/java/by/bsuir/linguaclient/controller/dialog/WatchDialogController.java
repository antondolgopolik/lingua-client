package by.bsuir.linguaclient.controller.dialog;

import by.bsuir.linguaclient.dto.lingua.SubtitleDto;
import by.bsuir.linguaclient.dto.lingua.VideoContentDetailsDto;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
@Scope("prototype")
@FxmlView("/fxml/WatchDialogView.fxml")
@Slf4j
public class WatchDialogController extends Dialog<Pair<VideoContentDetailsDto.VideoContentLocDto, SubtitleDto>> implements Initializable {

    @FXML
    private DialogPane dialogPane;
    @FXML
    private ChoiceBox<VideoContentDetailsDto.VideoContentLocDto> firstLanguageChoiceBox;
    @FXML
    private ChoiceBox<SubtitleDto> secondLanguageChoiceBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        firstLanguageChoiceBox.valueProperty().addListener(this::firstLanguageChoiceBoxValueChangeHandler);
        setDialogPane(dialogPane);
        setResultConverter(this::formResult);
    }

    private void firstLanguageChoiceBoxValueChangeHandler(ObservableValue<? extends VideoContentDetailsDto.VideoContentLocDto> observableValue, VideoContentDetailsDto.VideoContentLocDto oldV, VideoContentDetailsDto.VideoContentLocDto newV) {
        secondLanguageChoiceBox.setItems(FXCollections.observableList(newV.getSubtitles()));
    }

    private Pair<VideoContentDetailsDto.VideoContentLocDto, SubtitleDto> formResult(ButtonType buttonType) {
        if (buttonType.getButtonData().isCancelButton()) {
            return null;
        }
        return new Pair<>(firstLanguageChoiceBox.getValue(), secondLanguageChoiceBox.getValue());
    }

    public void setVideoContentDetailsDto(VideoContentDetailsDto videoContentDetailsDto) {
        firstLanguageChoiceBox.getItems().addAll(videoContentDetailsDto.getVideoContentLocDtos());
    }
}

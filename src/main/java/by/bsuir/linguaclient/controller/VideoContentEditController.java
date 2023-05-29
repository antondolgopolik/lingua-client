package by.bsuir.linguaclient.controller;

import by.bsuir.linguaclient.api.lingua.LinguaClient;
import by.bsuir.linguaclient.dto.lingua.GenreDto;
import by.bsuir.linguaclient.dto.lingua.VideoContentEditFormDto;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Component
@FxmlView("/fxml/VideoContentEditView.fxml")
@Slf4j
public class VideoContentEditController implements Initializable {

    @FXML
    private TextField nameTextField;
    @FXML
    private TextArea shortDescriptionTextArea;
    @FXML
    private TextArea descriptionTextArea;
    @FXML
    private TextField durationTextField;
    @FXML
    private Button addGenreButton;
    @FXML
    private ChoiceBox<GenreDto> genreChoiceBox;
    @FXML
    private Button deleteGenreButton;
    @FXML
    private ListView<GenreDto> genreListView;
    @FXML
    private Button cancelButton;
    @FXML
    private Button saveButton;

    private final FxWeaver fxWeaver;
    private final LinguaClient linguaClient;

    private VideoContentEditFormDto videoContentEditFormDto;

    public VideoContentEditController(FxWeaver fxWeaver, LinguaClient linguaClient) {
        this.fxWeaver = fxWeaver;
        this.linguaClient = linguaClient;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void fill(UUID videoContentId) {
        try {
            this.videoContentEditFormDto = linguaClient.getVideoContentEditFormDto(videoContentId).get();
            nameTextField.setText(videoContentEditFormDto.getName());
            shortDescriptionTextArea.setText(videoContentEditFormDto.getShortDescription());
            descriptionTextArea.setText(videoContentEditFormDto.getDescription());
            durationTextField.setText(videoContentEditFormDto.getDuration().toString());
            genreListView.getItems().addAll(videoContentEditFormDto.getGenres());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}

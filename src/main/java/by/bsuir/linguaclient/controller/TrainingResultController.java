package by.bsuir.linguaclient.controller;

import by.bsuir.linguaclient.api.lingua.LinguaClient;
import by.bsuir.linguaclient.dto.lingua.DictionaryWordDto;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Component
@FxmlView("/fxml/TrainingResultView.fxml")
@Slf4j
public class TrainingResultController implements Initializable {

    @FXML
    private Button backToDictionaryButton;
    @FXML
    private TableView<TrainingResultItem> resultTableView;
    @FXML
    private TableColumn<TrainingResultItem, String> questionTableColumn;
    @FXML
    private TableColumn<TrainingResultItem, String> yourAnswerTableColumn;
    @FXML
    private TableColumn<TrainingResultItem, String> correctAnswerTableColumn;
    @FXML
    private TableColumn<TrainingResultItem, Integer> transcriptionTableColumn;

    private final FxWeaver fxWeaver;
    private final LinguaClient linguaClient;

    public TrainingResultController(FxWeaver fxWeaver, LinguaClient linguaClient) {
        this.fxWeaver = fxWeaver;
        this.linguaClient = linguaClient;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        questionTableColumn.setCellValueFactory(new PropertyValueFactory<>("question"));
        yourAnswerTableColumn.setCellValueFactory(new PropertyValueFactory<>("answer"));
        correctAnswerTableColumn.setCellValueFactory(new PropertyValueFactory<>("correctAnswer"));
        transcriptionTableColumn.setCellValueFactory(new PropertyValueFactory<>("transcription"));
    }

    public void fill(List<TrainingResultItem> trainingResultItems) {
        resultTableView.getItems().addAll(trainingResultItems);
    }

    @Data
    public static class TrainingResultItem {
        private String question;
        private String answer;
        private String correctAnswer;
        private String transcription;
        private Boolean correct;
    }
}

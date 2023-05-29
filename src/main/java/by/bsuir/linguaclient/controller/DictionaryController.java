package by.bsuir.linguaclient.controller;

import by.bsuir.linguaclient.api.lingua.LinguaClient;
import by.bsuir.linguaclient.dto.lingua.DictionaryDto;
import by.bsuir.linguaclient.dto.lingua.DictionaryWordDto;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxControllerAndView;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

@Component
@Scope("prototype")
@FxmlView("/fxml/DictionaryView.fxml")
@Slf4j
public class DictionaryController implements Initializable {

    @FXML
    private Button backToDictionariesButton;
    @FXML
    private TextField searchTextField;
    @FXML
    private Button searchButton;
    @FXML
    private Button startTrainingButton;
    @FXML
    private TableView<DictionaryWordDto> wordTableView;
    @FXML
    private TableColumn<DictionaryWordDto, String> firstLangTableColumn;
    @FXML
    private TableColumn<DictionaryWordDto, String> secondLangTableColumn;
    @FXML
    private TableColumn<DictionaryWordDto, String> transcriptionTableColumn;
    @FXML
    private TableColumn<DictionaryWordDto, Integer> masteryTableColumn;

    private final FxWeaver fxWeaver;
    private final LinguaClient linguaClient;

    private DictionaryDto dictionaryDto;

    public DictionaryController(FxWeaver fxWeaver, LinguaClient linguaClient) {
        this.fxWeaver = fxWeaver;
        this.linguaClient = linguaClient;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        firstLangTableColumn.setCellValueFactory(new PropertyValueFactory<>("firstLanguageText"));
        secondLangTableColumn.setCellValueFactory(new PropertyValueFactory<>("secondLanguageText"));
        transcriptionTableColumn.setCellValueFactory(new PropertyValueFactory<>("transcription"));
        masteryTableColumn.setCellValueFactory(new PropertyValueFactory<>("mastery"));

        backToDictionariesButton.setOnAction(event -> backToDictionariesButton.getScene().setRoot(fxWeaver.loadView(DictionaryListController.class)));
        startTrainingButton.setOnAction(event -> {
            TextInputDialog textInputDialog = new TextInputDialog();
            textInputDialog.setHeaderText("Enter size of training");
            textInputDialog.setContentText("Size of training:");
            textInputDialog.showAndWait().map(Integer::parseInt).ifPresent(size -> {
                try {
                    List<DictionaryWordDto> dictionaryWordDtos = linguaClient.getTraining(dictionaryDto.getId(), size).get();
                    FxControllerAndView<TrainingController, Parent> controllerAndView = fxWeaver.load(TrainingController.class);
                    controllerAndView.getController().fill(dictionaryWordDtos);
                    startTrainingButton.getScene().setRoot(controllerAndView.getView().orElseThrow());
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    public void fill(DictionaryDto dictionaryDto) {
        this.dictionaryDto = dictionaryDto;
        linguaClient.getDictionaryWordDtos(dictionaryDto.getId()).thenAcceptAsync(dictionaryWordDtos -> Platform.runLater(() -> {
            startTrainingButton.setDisable(false);
            wordTableView.getItems().addAll(dictionaryWordDtos);
        }));
    }
}

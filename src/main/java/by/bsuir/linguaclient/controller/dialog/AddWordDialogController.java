package by.bsuir.linguaclient.controller.dialog;

import by.bsuir.linguaclient.api.lingua.LinguaClient;
import by.bsuir.linguaclient.dto.lingua.AddWordToDictionaryFormDto;
import by.bsuir.linguaclient.dto.lingua.DictionaryDto;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Pair;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
@Scope("prototype")
@FxmlView("/fxml/AddWordDialogView.fxml")
@Slf4j
public class AddWordDialogController extends Dialog<Pair<Long, AddWordToDictionaryFormDto>> implements Initializable {

    @FXML
    private DialogPane dialogPane;
    @FXML
    private TextField firstLangTextTextField;
    @FXML
    private TextField secondLangTextTextField;
    @FXML
    private TextField transcriptionTextField;
    @FXML
    private ChoiceBox<DictionaryDto> dictionaryChoiceBox;

    private final LinguaClient linguaClient;

    public AddWordDialogController(LinguaClient linguaClient) {
        this.linguaClient = linguaClient;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setDialogPane(dialogPane);
        setResultConverter(this::formResult);
        dictionaryChoiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(DictionaryDto dictionaryDto) {
                if (dictionaryDto == null) {
                    return "";
                }
                return dictionaryDto.getName();
            }

            @Override
            public DictionaryDto fromString(String s) {
                return null;
            }
        });
        linguaClient.getDictionariesDtos().thenAcceptAsync(dictionaryDtos -> Platform.runLater(() -> dictionaryChoiceBox.setItems(FXCollections.observableList(dictionaryDtos))));
    }

    private Pair<Long, AddWordToDictionaryFormDto> formResult(ButtonType buttonType) {
        if (buttonType.getButtonData().isCancelButton()) {
            return null;
        }
        AddWordToDictionaryFormDto addWordToDictionaryFormDto = new AddWordToDictionaryFormDto();
        addWordToDictionaryFormDto.setFirstLanguageText(firstLangTextTextField.getText());
        addWordToDictionaryFormDto.setSecondLanguageText(secondLangTextTextField.getText());
        addWordToDictionaryFormDto.setTranscription(transcriptionTextField.getText());
        return new Pair<>(dictionaryChoiceBox.getValue().getId(), addWordToDictionaryFormDto);
    }

    public void fill(AddWordToDictionaryFormDto addWordToDictionaryFormDto) {
        firstLangTextTextField.setText(addWordToDictionaryFormDto.getFirstLanguageText());
        secondLangTextTextField.setText(addWordToDictionaryFormDto.getSecondLanguageText());
        transcriptionTextField.setText(addWordToDictionaryFormDto.getTranscription());
    }
}

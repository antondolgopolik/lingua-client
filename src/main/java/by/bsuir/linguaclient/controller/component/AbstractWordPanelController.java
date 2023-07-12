package by.bsuir.linguaclient.controller.component;

import by.bsuir.linguaclient.api.lingua.LinguaClient;
import by.bsuir.linguaclient.controller.dialog.AddWordDialogController;
import by.bsuir.linguaclient.dto.dictionary.*;
import by.bsuir.linguaclient.dto.lingua.AddWordToDictionaryFormDto;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import net.rgielen.fxweaver.core.FxWeaver;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

public abstract class AbstractWordPanelController implements Initializable {

    @FXML
    private HBox topBarHBox;
    @FXML
    private Button addToDictionaryButton;
    @FXML
    private TreeView<String> wordTreeView;

    private final FxWeaver fxWeaver;
    private final LinguaClient linguaClient;

    private DicResultDto currentDicResultDto;

    protected AbstractWordPanelController(FxWeaver fxWeaver, LinguaClient linguaClient) {
        this.fxWeaver = fxWeaver;
        this.linguaClient = linguaClient;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addToDictionaryButton.setOnAction(event -> {
            AddWordToDictionaryFormDto addWordToDictionaryFormDto = new AddWordToDictionaryFormDto();
            DefinitionDto definitionDto = currentDicResultDto.getDefinitions().get(0);
            addWordToDictionaryFormDto.setFirstLanguageText(definitionDto.getText());
            addWordToDictionaryFormDto.setSecondLanguageText(definitionDto.getTranslations().get(0).getText());
            addWordToDictionaryFormDto.setTranscription(definitionDto.getTranscription());

            AddWordDialogController addWordDialogController = fxWeaver.loadController(AddWordDialogController.class);
            addWordDialogController.fill(addWordToDictionaryFormDto);
            addWordDialogController.showAndWait().ifPresent(result -> {
                try {
                    boolean success = linguaClient.addWordToDictionary(result.getKey(), result.getValue()).get();
                    Alert alert;
                    if (success) {
                        alert = new Alert(Alert.AlertType.INFORMATION, "Word was successfully added", ButtonType.OK);
                    } else {
                        alert = new Alert(Alert.AlertType.WARNING, "Word wasn't added, some error occurred", ButtonType.OK);
                    }
                    alert.getDialogPane().getStylesheets().add(getClass().getResource("/css/AddWordDialogViewStyle.css").toExternalForm());
                    alert.showAndWait();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    public void fill(DicResultDto dicResultDto) {
        TreeItem<String> root = new TreeItem<>("Definitions");
        root.setExpanded(true);
        ObservableList<TreeItem<String>> children = root.getChildren();
        List<DefinitionDto> definitions = dicResultDto.getDefinitions();
        if (definitions != null) {
            for (DefinitionDto definition : definitions) {
                children.add(fillDefinition(definition));
            }
        }
        wordTreeView.setRoot(root);
        addToDictionaryButton.setDisable(false);
        currentDicResultDto = dicResultDto;
    }

    private TreeItem<String> fillDefinition(DefinitionDto definitionDto) {
        TreeItem<String> item = new TreeItem<>(definitionDto.getText() + ", [" + definitionDto.getTranscription() + "], " + definitionDto.getPartOfSpeech());
        item.setExpanded(true);
        ObservableList<TreeItem<String>> children = item.getChildren();
        List<TranslationDto> translations = definitionDto.getTranslations();
        if (translations != null) {
            for (TranslationDto translation : translations) {
                children.add(fillTranslation(translation));
            }
        }
        return item;
    }

    private TreeItem<String> fillTranslation(TranslationDto translationDto) {
        TreeItem<String> item = new TreeItem<>(translationDto.getText() + ", " + translationDto.getPartOfSpeech());

        ObservableList<TreeItem<String>> children = item.getChildren();
        TreeItem<String> synonymsItem = new TreeItem<>("Synonyms");
        TreeItem<String> meansItem = new TreeItem<>("Means");
        TreeItem<String> examplesItem = new TreeItem<>("Examples");
        children.addAll(synonymsItem, meansItem, examplesItem);

        ObservableList<TreeItem<String>> synonymsItemChildren = synonymsItem.getChildren();
        List<SynonymDto> synonyms = translationDto.getSynonyms();
        if (synonyms != null) {
            for (SynonymDto synonym : synonyms) {
                synonymsItemChildren.add(fillSynonym(synonym));
            }
        }
        ObservableList<TreeItem<String>> meansItemChildren = meansItem.getChildren();
        List<MeanDto> means = translationDto.getMeans();
        if (means != null) {
            for (MeanDto mean : means) {
                meansItemChildren.add(fillMean(mean));
            }
        }
        ObservableList<TreeItem<String>> examplesItemChildren = examplesItem.getChildren();
        List<ExampleDto> examples = translationDto.getExamples();
        if (examples != null) {
            for (ExampleDto example : examples) {
                examplesItemChildren.add(fillExample(example));
            }
        }

        return item;
    }

    private TreeItem<String> fillSynonym(SynonymDto synonymDto) {
        return new TreeItem<>(synonymDto.getText() + ", " + synonymDto.getPartOfSpeech());
    }

    private TreeItem<String> fillMean(MeanDto meanDto) {
        return new TreeItem<>(meanDto.getText());
    }

    private TreeItem<String> fillExample(ExampleDto exampleDto) {
        TreeItem<String> item = new TreeItem<>(exampleDto.getText());
        ObservableList<TreeItem<String>> children = item.getChildren();
        List<TranslationDto> translations = exampleDto.getTranslations();
        if (translations != null) {
            for (TranslationDto translation : translations) {
                children.add(fillTranslation(translation));
            }
        }
        return item;
    }
}

package by.bsuir.linguaclient.controller.component;

import by.bsuir.linguaclient.dto.dictionary.*;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Component
@Scope("prototype")
@FxmlView("/fxml/WordPanelView.fxml")
@Slf4j
public class WordPanelController implements Initializable {

    @FXML
    private VBox rootVBox;
    @FXML
    private Button addToDictionaryButton;
    @FXML
    private TreeView<String> wordTreeView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

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

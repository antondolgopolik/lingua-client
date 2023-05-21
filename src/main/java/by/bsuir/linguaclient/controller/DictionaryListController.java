package by.bsuir.linguaclient.controller;

import by.bsuir.linguaclient.api.lingua.LinguaClient;
import by.bsuir.linguaclient.dto.lingua.DictionaryDto;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxControllerAndView;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
@Scope("prototype")
@FxmlView("/fxml/DictionaryListView.fxml")
@Slf4j
public class DictionaryListController implements Initializable {

    @FXML
    private Button backToCatalogButton;
    @FXML
    private TextField searchTextField;
    @FXML
    private Button searchButton;
    @FXML
    private Button openButton;
    @FXML
    private Button createNewDictionaryButton;
    @FXML
    private TableView<DictionaryDto> dictionaryTableView;
    @FXML
    private TableColumn<DictionaryDto, String> nameTableColumn;
    @FXML
    private TableColumn<DictionaryDto, String> firstLangTableColumn;
    @FXML
    private TableColumn<DictionaryDto, String> secondLangTableColumn;
    @FXML
    private TableColumn<DictionaryDto, Integer> sizeTableColumn;

    private final FxWeaver fxWeaver;
    private final LinguaClient linguaClient;

    public DictionaryListController(FxWeaver fxWeaver, LinguaClient linguaClient) {
        this.fxWeaver = fxWeaver;
        this.linguaClient = linguaClient;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        nameTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        firstLangTableColumn.setCellValueFactory(new PropertyValueFactory<>("firstLanguage"));
        secondLangTableColumn.setCellValueFactory(new PropertyValueFactory<>("secondLanguage"));
        sizeTableColumn.setCellValueFactory(new PropertyValueFactory<>("size"));

        dictionaryTableView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldV, newV) -> openButton.setDisable(newV == null));
        backToCatalogButton.setOnAction(event -> backToCatalogButton.getScene().setRoot(fxWeaver.loadView(CatalogController.class)));
        openButton.setOnAction(event -> {
            FxControllerAndView<DictionaryController, Parent> controllerAndView = fxWeaver.load(DictionaryController.class);
            controllerAndView.getController().fill(dictionaryTableView.getSelectionModel().getSelectedItem());
            openButton.getScene().setRoot(controllerAndView.getView().orElseThrow());
        });

        linguaClient.getDictionariesDtos().thenAcceptAsync(dictionaryDtos -> Platform.runLater(() -> dictionaryTableView.getItems().addAll(dictionaryDtos)));
    }
}

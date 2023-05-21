package by.bsuir.linguaclient.controller;

import by.bsuir.linguaclient.api.lingua.LinguaClient;
import by.bsuir.linguaclient.controller.component.DuoWatchRequestCatalogItemPageController;
import by.bsuir.linguaclient.dto.lingua.DuoWatchRequestCatalogItemPageDto;
import by.bsuir.linguaclient.dto.lingua.LanguageDto;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Pagination;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxControllerAndView;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

@Component
@Scope("prototype")
@FxmlView("/fxml/DuoWatchRequestCatalogView.fxml")
@Slf4j
public class DuoWatchRequestCatalogController implements Initializable {

    @FXML
    private Button backToCatalogButton;
    @FXML
    private TextField searchTextField;
    @FXML
    private ChoiceBox<LanguageDto> videoContentLangChoiceBox;
    @FXML
    private ChoiceBox<LanguageDto> secondLangChoiceBox;
    @FXML
    private Button searchButton;
    @FXML
    private Pagination catalogItemPagination;

    private final FxWeaver fxWeaver;
    private final LinguaClient linguaClient;

    public DuoWatchRequestCatalogController(FxWeaver fxWeaver,
                                            LinguaClient linguaClient) {
        this.fxWeaver = fxWeaver;
        this.linguaClient = linguaClient;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        backToCatalogButton.setOnAction(event -> backToCatalogButton.getScene().setRoot(fxWeaver.loadView(CatalogController.class)));
        searchTextField.setOnAction(event -> search());
        linguaClient.getAllLanguages().thenAcceptAsync(languageDtos -> Platform.runLater(() -> {
            videoContentLangChoiceBox.getItems().addAll(languageDtos);
            secondLangChoiceBox.getItems().addAll(languageDtos);
        }));
        search();
    }

    private void search() {
        catalogItemPagination.setPageFactory(page -> searchPageFactory(searchTextField.getText(), page));
    }

    private Node searchPageFactory(String searchQuery, Integer page) {
        try {
            LanguageDto videoContentLang = videoContentLangChoiceBox.getValue();
            LanguageDto secondLang = secondLangChoiceBox.getValue();
            DuoWatchRequestCatalogItemPageDto pageDto = linguaClient.duoWatchRequestCatalogSearch(
                    searchQuery,
                    videoContentLang != null ? videoContentLang.getId() : null,
                    secondLang != null ? secondLang.getId() : null,
                    page, 15
            ).get();

            Integer totalPages = pageDto.getTotalPages();
            if (totalPages != 0) {
                catalogItemPagination.setVisible(true);
                catalogItemPagination.setPageCount(totalPages);
            } else {
                catalogItemPagination.setVisible(false);
            }

            FxControllerAndView<DuoWatchRequestCatalogItemPageController, Node> controllerAndView = fxWeaver.load(DuoWatchRequestCatalogItemPageController.class);
            controllerAndView.getController().fill(pageDto.getDuoWatchRequestCatalogItemDtos());
            return controllerAndView.getView().orElseThrow();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}

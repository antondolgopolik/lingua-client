package by.bsuir.linguaclient.controller;

import by.bsuir.linguaclient.api.lingua.LinguaClient;
import by.bsuir.linguaclient.controller.component.CatalogItemPageController;
import by.bsuir.linguaclient.dto.lingua.CatalogItemPageDto;
import by.bsuir.linguaclient.dto.lingua.PlayerMessageDto;
import by.bsuir.linguaclient.dto.lingua.PlayerMessageType;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxControllerAndView;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

@Component
@Scope("prototype")
@FxmlView("/fxml/CatalogView.fxml")
@Slf4j
public class CatalogController implements Initializable {

    @FXML
    private MenuButton menuButton;
    @FXML
    private MenuItem dictionariesMenuItem;
    @FXML
    private MenuItem personalDuoWatchRequestsMenuItem;
    @FXML
    private MenuItem exploreDuoWatchRequestsMenuItem;
    @FXML
    private MenuItem logOutMenuItem;
    @FXML
    private TextField searchTextField;
    @FXML
    private Button searchButton;
    @FXML
    private Pagination catalogItemPagination;

    private final FxWeaver fxWeaver;
    private final LinguaClient linguaClient;

    public CatalogController(FxWeaver fxWeaver,
                             LinguaClient linguaClient) {
        this.fxWeaver = fxWeaver;
        this.linguaClient = linguaClient;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dictionariesMenuItem.setOnAction(event -> menuButton.getScene().setRoot(fxWeaver.loadView(DictionaryListController.class)));
        personalDuoWatchRequestsMenuItem.setOnAction(event -> menuButton.getScene().setRoot(fxWeaver.loadView(PersonalDuoWatchRequestController.class)));
        exploreDuoWatchRequestsMenuItem.setOnAction(event -> menuButton.getScene().setRoot(fxWeaver.loadView(DuoWatchRequestCatalogController.class)));
        logOutMenuItem.setOnAction(event -> {
            linguaClient.logOut();
            menuButton.getScene().setRoot(fxWeaver.loadView(LoginController.class));
        });
        searchTextField.setOnAction(event -> search());
        search();
    }

    private void search() {
        catalogItemPagination.setPageFactory(page -> searchPageFactory(searchTextField.getText(), page));
    }

    private Node searchPageFactory(String searchQuery, Integer page) {
        try {
            CatalogItemPageDto pageDto = linguaClient.catalogSearch(
                    searchQuery,
                    page, 15
            ).get();

            Integer totalPages = pageDto.getTotalPages();
            if (totalPages != 0) {
                catalogItemPagination.setVisible(true);
                catalogItemPagination.setPageCount(totalPages);
            } else {
                catalogItemPagination.setVisible(false);
            }

            FxControllerAndView<CatalogItemPageController, Node> controllerAndView = fxWeaver.load(CatalogItemPageController.class);
            controllerAndView.getController().fill(pageDto.getCatalogItemDtos());
            return controllerAndView.getView().orElseThrow();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}

package by.bsuir.linguaclient.controller;

import by.bsuir.linguaclient.api.lingua.LinguaClient;
import by.bsuir.linguaclient.controller.component.CatalogItemPageController;
import by.bsuir.linguaclient.dto.lingua.CatalogItemPageDto;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Scope("prototype")
@FxmlView("/fxml/CatalogView.fxml")
@Slf4j
public class CatalogController implements Initializable {

    @FXML
    private MenuButton menuButton;
    @FXML
    private MenuItem duoWatchRequestsMenuItem;
    @FXML
    private MenuItem dictionariesMenuItem;
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
            CatalogItemPageDto catalogItemPageDto = linguaClient.catalogSearch(
                    searchQuery,
                    page, 15
            ).get();

            catalogItemPagination.setPageCount(catalogItemPageDto.getTotalPages());

            FxControllerAndView<CatalogItemPageController, Node> controllerAndView = fxWeaver.load(CatalogItemPageController.class);
            controllerAndView.getController().fill(catalogItemPageDto.getCatalogItemDtos());
            return controllerAndView.getView().orElseThrow();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}

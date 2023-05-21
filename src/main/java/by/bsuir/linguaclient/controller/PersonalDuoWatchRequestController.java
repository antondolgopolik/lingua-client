package by.bsuir.linguaclient.controller;

import by.bsuir.linguaclient.api.lingua.LinguaClient;
import by.bsuir.linguaclient.controller.component.DuoWatchRequestCatalogItemPageController;
import by.bsuir.linguaclient.controller.component.PersonalDuoWatchRequestItemPageController;
import by.bsuir.linguaclient.dto.lingua.DuoWatchRequestCatalogItemPageDto;
import by.bsuir.linguaclient.dto.lingua.DuoWatchRequestStatus;
import by.bsuir.linguaclient.dto.lingua.LanguageDto;
import by.bsuir.linguaclient.dto.lingua.PersonalDuoWatchRequestPageDto;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
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
@FxmlView("/fxml/PersonalDuoWatchRequestView.fxml")
@Slf4j
public class PersonalDuoWatchRequestController implements Initializable {

    @FXML
    private Button backToCatalogButton;
    @FXML
    private TextField searchTextField;
    @FXML
    private ChoiceBox<Role> roleChoiceBox;
    @FXML
    private ChoiceBox<DuoWatchRequestStatus> statusChoiceBox;
    @FXML
    private Button searchButton;
    @FXML
    private Pagination itemPagination;

    private final FxWeaver fxWeaver;
    private final LinguaClient linguaClient;

    public PersonalDuoWatchRequestController(FxWeaver fxWeaver,
                                             LinguaClient linguaClient) {
        this.fxWeaver = fxWeaver;
        this.linguaClient = linguaClient;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        backToCatalogButton.setOnAction(event -> backToCatalogButton.getScene().setRoot(fxWeaver.loadView(CatalogController.class)));
        searchTextField.setOnAction(event -> search());
        roleChoiceBox.valueProperty().addListener((observableValue, oldV, newV) -> {
            if (newV == Role.REQUESTER) {
                statusChoiceBox.setItems(FXCollections.observableArrayList(DuoWatchRequestStatus.values()));
            } else {
                statusChoiceBox.setItems(FXCollections.observableArrayList(DuoWatchRequestStatus.ON_HOLD, DuoWatchRequestStatus.CLOSED));
            }
        });
        roleChoiceBox.getItems().addAll(Role.values());
        roleChoiceBox.setValue(Role.REQUESTER);
        search();
    }

    private void search() {
        itemPagination.setPageFactory(page -> searchPageFactory(searchTextField.getText(), page));
    }

    private Node searchPageFactory(String searchQuery, Integer page) {
        try {
            boolean owner = roleChoiceBox.getValue() == Role.REQUESTER;
            DuoWatchRequestStatus status = statusChoiceBox.getValue();
            PersonalDuoWatchRequestPageDto pageDto = linguaClient.duoWatchRequestPersonalSearch(
                    searchQuery, owner, status, page, 15
            ).get();

            Integer totalPages = pageDto.getTotalPages();
            if (totalPages != 0) {
                itemPagination.setVisible(true);
                itemPagination.setPageCount(totalPages);
            } else {
                itemPagination.setVisible(false);
            }

            FxControllerAndView<PersonalDuoWatchRequestItemPageController, Node> controllerAndView = fxWeaver.load(PersonalDuoWatchRequestItemPageController.class);
            controllerAndView.getController().fill(pageDto.getPersonalDuoWatchRequestDtos(), owner);
            return controllerAndView.getView().orElseThrow();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private enum Role {
        REQUESTER, RESPONDER
    }
}

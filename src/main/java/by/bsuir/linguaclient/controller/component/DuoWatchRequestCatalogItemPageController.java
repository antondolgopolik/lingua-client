package by.bsuir.linguaclient.controller.component;

import by.bsuir.linguaclient.dto.lingua.DuoWatchRequestCatalogItemDto;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxControllerAndView;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
@FxmlView("/fxml/DuoWatchRequestCatalogItemPageView.fxml")
@Slf4j
public class DuoWatchRequestCatalogItemPageController {

    @FXML
    private VBox catalogItemVBox;

    private final FxWeaver fxWeaver;

    public DuoWatchRequestCatalogItemPageController(FxWeaver fxWeaver) {
        this.fxWeaver = fxWeaver;
    }

    public void fill(List<DuoWatchRequestCatalogItemDto> duoWatchRequestCatalogItemDtos) {
        List<Node> catalogItems = new ArrayList<>();
        for (DuoWatchRequestCatalogItemDto duoWatchRequestCatalogItemDto : duoWatchRequestCatalogItemDtos) {
            FxControllerAndView<DuoWatchRequestCatalogItemController, Node> controllerAndView = fxWeaver.load(DuoWatchRequestCatalogItemController.class);
            controllerAndView.getController().fill(duoWatchRequestCatalogItemDto);
            catalogItems.add(controllerAndView.getView().orElseThrow());
        }
        catalogItemVBox.getChildren().addAll(catalogItems);
    }
}

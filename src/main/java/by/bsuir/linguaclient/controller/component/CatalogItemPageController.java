package by.bsuir.linguaclient.controller.component;

import by.bsuir.linguaclient.dto.lingua.CatalogItemDto;
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
@FxmlView("/fxml/CatalogItemPageView.fxml")
@Slf4j
public class CatalogItemPageController {

    @FXML
    private VBox catalogItemVBox;

    private final FxWeaver fxWeaver;

    public CatalogItemPageController(FxWeaver fxWeaver) {
        this.fxWeaver = fxWeaver;
    }

    public void fill(List<CatalogItemDto> catalogItemDtos) {
        List<Node> catalogItems = new ArrayList<>();
        for (CatalogItemDto catalogItemDto : catalogItemDtos) {
            FxControllerAndView<CatalogItemController, Node> controllerAndView = fxWeaver.load(CatalogItemController.class);
            controllerAndView.getController().fill(catalogItemDto);
            catalogItems.add(controllerAndView.getView().orElseThrow());
        }
        catalogItemVBox.getChildren().addAll(catalogItems);
    }
}

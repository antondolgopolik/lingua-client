package by.bsuir.linguaclient.controller.component;

import by.bsuir.linguaclient.api.lingua.LinguaClient;
import by.bsuir.linguaclient.controller.VideoContentDetailsController;
import by.bsuir.linguaclient.controller.VideoContentEditController;
import by.bsuir.linguaclient.dto.lingua.CatalogItemDto;
import by.bsuir.linguaclient.dto.lingua.GenreDto;
import by.bsuir.linguaclient.dto.lingua.Role;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxControllerAndView;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ResourceBundle;

@Component
@Scope("prototype")
@FxmlView("/fxml/CatalogItemView.fxml")
@Slf4j
public class CatalogItemController implements Initializable {

    @FXML
    private StackPane posterStackPane;
    @FXML
    private ImageView posterImageView;
    @FXML
    private Label durationLabel;
    @FXML
    private Label viewsLabel;
    @FXML
    private Label genresLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private Label shortDescriptionLabel;
    @FXML
    private Button goToDetailsButton;

    private final FxWeaver fxWeaver;
    private final LinguaClient linguaClient;

    private CatalogItemDto catalogItemDto;

    public CatalogItemController(FxWeaver fxWeaver,
                                 LinguaClient linguaClient) {
        this.fxWeaver = fxWeaver;
        this.linguaClient = linguaClient;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        goToDetailsButton.setOnAction(event -> {
            List<Role> roles = linguaClient.getRoles();
            if (!roles.contains(Role.ROLE_CONTENT_MANAGER)) {
                FxControllerAndView<VideoContentDetailsController, Parent> controllerAndView = fxWeaver.load(VideoContentDetailsController.class);
                controllerAndView.getController().fill(catalogItemDto.getId());
                goToDetailsButton.getScene().setRoot(controllerAndView.getView().orElseThrow());
            } else {
                FxControllerAndView<VideoContentEditController, Parent> controllerAndView = fxWeaver.load(VideoContentEditController.class);
                controllerAndView.getController().fill(catalogItemDto.getId());
                goToDetailsButton.getScene().setRoot(controllerAndView.getView().orElseThrow());
            }
        });
    }

    public void fill(CatalogItemDto catalogItemDto) {
        this.catalogItemDto = catalogItemDto;
        posterImageView.fitHeightProperty().bind(posterStackPane.heightProperty());
        posterImageView.setImage(linguaClient.getImage(catalogItemDto.getId(), 0, 400));
        durationLabel.setText("Duration: " + buildDuration(catalogItemDto.getDuration()));
        viewsLabel.setText("Views: " + catalogItemDto.getViews());
        genresLabel.setText(buildGenresString(catalogItemDto.getGenres()));
        nameLabel.setText(catalogItemDto.getName());
        shortDescriptionLabel.setText(catalogItemDto.getShortDescription());
    }

    private String buildDuration(Integer duration) {
        long h = duration / 60;
        long m = duration % 60;
        return String.format("%dh %dm", h, m);
    }

    private String buildGenresString(List<GenreDto> genres) {
        if (genres.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder("Genres: ");
        for (GenreDto genre : genres) {
            builder.append(genre.getName()).append(" | ");
        }
        builder.setLength(builder.length() - 3);
        return builder.toString();
    }
}

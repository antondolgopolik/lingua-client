package by.bsuir.linguaclient.controller.component;

import by.bsuir.linguaclient.api.lingua.LinguaClient;
import by.bsuir.linguaclient.dto.lingua.DuoWatchRequestCatalogItemDto;
import by.bsuir.linguaclient.dto.lingua.GenreDto;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

@Component
@Scope("prototype")
@FxmlView("/fxml/DuoWatchRequestCatalogItemView.fxml")
@Slf4j
public class DuoWatchRequestCatalogItemController implements Initializable {

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
    private Button acceptButton;
    @FXML
    private Label nameLabel;
    @FXML
    private Label shortDescriptionLabel;
    @FXML
    private Label videoContentLangLabel;
    @FXML
    private Label secondLangLabel;

    private final FxWeaver fxWeaver;
    private final LinguaClient linguaClient;

    private DuoWatchRequestCatalogItemDto duoWatchRequestCatalogItemDto;

    public DuoWatchRequestCatalogItemController(FxWeaver fxWeaver,
                                                LinguaClient linguaClient) {
        this.fxWeaver = fxWeaver;
        this.linguaClient = linguaClient;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        acceptButton.setOnAction(event -> {
            acceptButton.setDisable(true);
            try {
                boolean success = linguaClient.acceptDuoWatchRequest(duoWatchRequestCatalogItemDto.getId()).get();
                Alert alert;
                if (success) {
                    alert = new Alert(Alert.AlertType.INFORMATION, "Duo Watch Response was successfully created", ButtonType.OK);
                } else {
                    alert = new Alert(Alert.AlertType.WARNING, "Duo Watch Response wasn't created", ButtonType.OK);
                }
                alert.getDialogPane().getStylesheets().add(getClass().getResource("/css/AddWordDialogViewStyle.css").toExternalForm());
                alert.showAndWait();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void fill(DuoWatchRequestCatalogItemDto duoWatchRequestCatalogItemDto) {
        this.duoWatchRequestCatalogItemDto = duoWatchRequestCatalogItemDto;
        var videoContentLocDto = duoWatchRequestCatalogItemDto.getVideoContentLocDto();
        var catalogItemDto = videoContentLocDto.getCatalogItemDto();
        posterImageView.fitHeightProperty().bind(posterStackPane.heightProperty());
        posterImageView.setImage(linguaClient.getImage(catalogItemDto.getId(), 0, 400));
        durationLabel.setText("Duration: " + buildDuration(catalogItemDto.getDuration()));
        viewsLabel.setText("Views: " + catalogItemDto.getViews());
        genresLabel.setText(buildGenresString(catalogItemDto.getGenres()));
        nameLabel.setText(catalogItemDto.getName());
        shortDescriptionLabel.setText(catalogItemDto.getShortDescription());
        videoContentLangLabel.setText("Video content language: " + videoContentLocDto.getLanguage().getName());
        secondLangLabel.setText("Second language: " + duoWatchRequestCatalogItemDto.getSecondLanguage().getName());
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

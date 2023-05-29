package by.bsuir.linguaclient.controller;

import by.bsuir.linguaclient.api.lingua.LinguaClient;
import by.bsuir.linguaclient.controller.dialog.WatchDialogController;
import by.bsuir.linguaclient.dto.lingua.CreateDuoWatchRequestFormDto;
import by.bsuir.linguaclient.dto.lingua.GenreDto;
import by.bsuir.linguaclient.dto.lingua.SubtitleDto;
import by.bsuir.linguaclient.dto.lingua.VideoContentDetailsDto;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxControllerAndView;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Component
@FxmlView("/fxml/VideoContentDetailsView.fxml")
@Slf4j
public class VideoContentDetailsController implements Initializable {

    @FXML
    private Button backToCatalogButton;
    @FXML
    private Label nameLabel;
    @FXML
    private Label descriptionLabel;
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
    private Button watchButton;
    @FXML
    private Button duoWatchButton;

    private final FxWeaver fxWeaver;
    private final LinguaClient linguaClient;

    private VideoContentDetailsDto videoContentDetailsDto;

    public VideoContentDetailsController(FxWeaver fxWeaver,
                                         LinguaClient linguaClient) {
        this.fxWeaver = fxWeaver;
        this.linguaClient = linguaClient;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        backToCatalogButton.setOnAction(this::menuButtonActionHandler);
        watchButton.setOnAction(this::watchButtonActionHandler);
        duoWatchButton.setOnAction(this::duoWatchButtonActionHandler);
    }

    private void menuButtonActionHandler(ActionEvent event) {
        backToCatalogButton.getScene().setRoot(fxWeaver.loadView(CatalogController.class));
    }

    private void watchButtonActionHandler(ActionEvent event) {
        WatchDialogController watchDialogController = fxWeaver.loadController(WatchDialogController.class);
        watchDialogController.setVideoContentDetailsDto(videoContentDetailsDto);
        watchDialogController.showAndWait().ifPresent(result -> {
            FxControllerAndView<PlayerController, Parent> controllerAndView = fxWeaver.load(PlayerController.class);
            PlayerController playerController = controllerAndView.getController();
            var videoContentLocDto = result.getKey();
            var subtitleDto = result.getValue();
            playerController.fill(
                    null,
                    videoContentLocDto.getId(), subtitleDto.getId(),
                    videoContentLocDto.getLanguage(),
                    subtitleDto.getSecondLanguage(), watchButton.getScene()
            );
            watchButton.getScene().setRoot(controllerAndView.getView().orElseThrow());
        });
    }

    private void duoWatchButtonActionHandler(ActionEvent event) {
        WatchDialogController watchDialogController = fxWeaver.loadController(WatchDialogController.class);
        watchDialogController.setVideoContentDetailsDto(videoContentDetailsDto);
        watchDialogController.showAndWait().ifPresent(result -> {
            var form = new CreateDuoWatchRequestFormDto();
            form.setVideoContentLocId(result.getKey().getId());
            form.setSecondLangId(result.getValue().getSecondLanguage().getId());
            try {
                boolean success = linguaClient.createDuoWatchRequest(form).get();
                Alert alert;
                if (success) {
                    alert = new Alert(Alert.AlertType.INFORMATION, "Duo Watch Request was successfully created", ButtonType.OK);
                } else {
                    alert = new Alert(Alert.AlertType.WARNING, "Duo Watch Request wasn't created as you already have request for given video content localization", ButtonType.OK);
                }
                alert.showAndWait();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void fill(UUID videoContentId) {
        try {
            this.videoContentDetailsDto = linguaClient.getVideoContentDetails(videoContentId).get();
            nameLabel.setText(videoContentDetailsDto.getName());
            descriptionLabel.setText(videoContentDetailsDto.getDescription());
            posterImageView.fitHeightProperty().bind(posterStackPane.heightProperty());
            posterImageView.setImage(linguaClient.getImage(videoContentDetailsDto.getId(), 0, 400));
            durationLabel.setText("Duration: " + videoContentDetailsDto.getDuration());
            viewsLabel.setText("Views: " + videoContentDetailsDto.getViews());
            genresLabel.setText(buildGenresString(videoContentDetailsDto.getGenres()));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
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

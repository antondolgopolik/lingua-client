package by.bsuir.linguaclient.controller.component;

import by.bsuir.linguaclient.api.lingua.LinguaClient;
import by.bsuir.linguaclient.application.LinguaClientApplication;
import by.bsuir.linguaclient.dto.lingua.GenreDto;
import by.bsuir.linguaclient.dto.lingua.PersonalDuoWatchRequestDto;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.apache.commons.text.WordUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
@FxmlView("/fxml/PersonalDuoWatchRequestItemInfoView.fxml")
@Slf4j
public class PersonalDuoWatchRequestItemInfoController {

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
    private Label videoContentLangLabel;
    @FXML
    private Label secondLangLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private HBox partnerHBox;
    @FXML
    private Hyperlink partnerHyperlink;

    private final LinguaClient linguaClient;

    public PersonalDuoWatchRequestItemInfoController(LinguaClient linguaClient) {
        this.linguaClient = linguaClient;
    }

    public void fill(PersonalDuoWatchRequestDto personalDuoWatchRequestDto) {
        var videoContentLocDto = personalDuoWatchRequestDto.getVideoContentLocDto();
        var catalogItemDto = videoContentLocDto.getCatalogItemDto();
        posterImageView.fitHeightProperty().bind(posterStackPane.heightProperty());
        posterImageView.setImage(linguaClient.getImage(catalogItemDto.getId(), 0, 400));
        durationLabel.setText("Duration: " + buildDuration(catalogItemDto.getDuration()));
        viewsLabel.setText("Views: " + catalogItemDto.getViews());
        genresLabel.setText(buildGenresString(catalogItemDto.getGenres()));
        nameLabel.setText(catalogItemDto.getName());
        videoContentLangLabel.setText("Video content language: " + videoContentLocDto.getLanguage().getName());
        secondLangLabel.setText("Second language: " + personalDuoWatchRequestDto.getSecondLanguage().getName());
        statusLabel.setText("Status: " + WordUtils.capitalizeFully(personalDuoWatchRequestDto.getStatus().toString().replace('_', ' ')));
        if (personalDuoWatchRequestDto.getPartnerTgUsername() != null) {
            String uri = "https://t.me/" + personalDuoWatchRequestDto.getPartnerTgUsername();
            partnerHyperlink.setText(uri);
            partnerHyperlink.setOnAction(event -> {
                HostServices hostServices = LinguaClientApplication.getInstance().getHostServices();
                hostServices.showDocument(uri);
            });
        } else {
            partnerHBox.setVisible(false);
        }
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

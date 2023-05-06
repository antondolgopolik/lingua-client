package by.bsuir.linguaclient.controller;

import by.bsuir.linguaclient.api.dictionary.DictionaryClient;
import by.bsuir.linguaclient.api.lingua.LinguaClient;
import by.bsuir.linguaclient.configuration.ResourceHolder;
import by.bsuir.linguaclient.controller.component.WordPanelController;
import by.bsuir.linguaclient.dto.lingua.SubtitleDto;
import by.bsuir.linguaclient.dto.lingua.VideoContentDetailsDto;
import by.bsuir.linguaclient.subtitle.Subtitle;
import by.bsuir.linguaclient.subtitle.SubtitleItem;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurface;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Scope("prototype")
@FxmlView("/fxml/PlayerView.fxml")
@Slf4j
public class PlayerController implements Initializable {

    @FXML
    private VBox playerVBox;
    @FXML
    private StackPane topBarStackPane;
    @FXML
    private Button menuButton;
    @FXML
    private StackPane imageViewStackPane;
    @FXML
    private ImageView imageView;
    @FXML
    private TextFlow textFlow1;
    @FXML
    private TextFlow textFlow2;
    @FXML
    private HBox botBarHBox;
    @FXML
    private Button backButton;
    @FXML
    private Button toggleButton;
    @FXML
    private ImageView toggleImageView;
    @FXML
    private Button forwardButton;
    @FXML
    private Slider timeSlider;
    @FXML
    private Label passedTimeLabel;
    @FXML
    private Label leftTimeLabel;
    @FXML
    private Button subButton;
    @FXML
    private ImageView subImageView;
    @FXML
    private Button soundButton;
    @FXML
    private ImageView soundImageView;
    @FXML
    private Slider soundSlider;

    @FXML
    private WordPanelController wordPanelController;
    @FXML
    private VBox wordPanel;

    private final FxWeaver fxWeaver;
    private final LinguaClient linguaClient;
    private final DictionaryClient dictionaryClient;
    private final ResourceHolder resourceHolder;

    private final MediaPlayerFactory mediaPlayerFactory;
    private final EmbeddedMediaPlayer embeddedMediaPlayer;

    private VideoContentDetailsDto.VideoContentLocDto videoContentLocDto;
    private SubtitleDto subtitleDto;
    private Subtitle subtitle;

    private final AtomicBoolean blockTimeSliderUpdate = new AtomicBoolean(false);
    private final AtomicBoolean blockPositionChange = new AtomicBoolean(false);
    private boolean controlBarsShown = true;
    private boolean wordPanelShown = false;

    private SubtitleItem currentSubtitleItem;

    public PlayerController(FxWeaver fxWeaver,
                            LinguaClient linguaClient,
                            DictionaryClient dictionaryClient,
                            ResourceHolder resourceHolder) {
        this.fxWeaver = fxWeaver;
        this.linguaClient = linguaClient;
        this.dictionaryClient = dictionaryClient;
        this.resourceHolder = resourceHolder;
        this.mediaPlayerFactory = new MediaPlayerFactory();
        this.embeddedMediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
        resourceHolder.setMediaPlayerFactory(mediaPlayerFactory);
        resourceHolder.setEmbeddedMediaPlayer(embeddedMediaPlayer);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        embeddedMediaPlayer.videoSurface().set(new ImageViewVideoSurface(imageView));
        embeddedMediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventListener());
        imageView.fitWidthProperty().bind(imageViewStackPane.widthProperty());
        imageView.fitHeightProperty().bind(imageViewStackPane.heightProperty());

        backButton.setOnAction(event -> {
            if (blockPositionChange.compareAndSet(false, true)) {
                long newTime = embeddedMediaPlayer.status().time() - 10 * 1000;
                embeddedMediaPlayer.controls().setTime(newTime);
                textFlow1.getChildren().clear();
                textFlow2.getChildren().clear();
                subtitle.back(newTime);
                blockPositionChange.set(false);
            }
        });
        forwardButton.setOnAction(event -> {
            if (blockPositionChange.compareAndSet(false, true)) {
                long newTime = embeddedMediaPlayer.status().time() + 10 * 1000;
                embeddedMediaPlayer.controls().setTime(newTime);
                textFlow1.getChildren().clear();
                textFlow2.getChildren().clear();
                subtitle.forward(newTime);
                blockPositionChange.set(false);
            }
        });
        toggleButton.setOnAction(event -> embeddedMediaPlayer.controls().pause());
        timeSlider.valueChangingProperty().addListener((observableValue, newV, oldV) -> {
            if (!newV) {
                setPositionFromTimeSliderValue(timeSlider.getValue());
            }
        });
        timeSlider.valueProperty().addListener((observableValue, newV, oldV) -> {
            if (timeSlider.isValueChanging()) {
                setPositionFromTimeSliderValue(timeSlider.getValue());
            }
        });
        timeSlider.setOnMousePressed(mouseEvent -> {
            timeSlider.setValueChanging(true);
            blockTimeSliderUpdate.set(true);
            setPositionFromTimeSliderValue(timeSlider.getValue());
        });
        timeSlider.setOnMouseReleased(mouseEvent -> {
            blockTimeSliderUpdate.set(false);
            timeSlider.setValueChanging(false);
        });
        soundButton.setOnAction(event -> embeddedMediaPlayer.audio().mute());
        soundSlider.valueProperty().addListener((observableValue, oldV, newV) -> embeddedMediaPlayer.audio().setVolume(newV.intValue()));
    }

    private void setPositionFromTimeSliderValue(double sliderValue) {
        if (blockPositionChange.compareAndSet(false, true)) {
            embeddedMediaPlayer.controls().setPosition((float) sliderValue / 100);
            textFlow1.getChildren().clear();
            textFlow2.getChildren().clear();
            subtitle.changePosition(embeddedMediaPlayer.status().time());
            blockPositionChange.set(false);
        }
    }

    private void setTimeSliderValueFromPosition(float position) {
        timeSlider.setValue(position * 100);
    }

    private void toggleControlBars() {
        if (controlBarsShown) {
            topBarStackPane.setVisible(false);
            botBarHBox.setVisible(false);
            controlBarsShown = false;
        } else {
            topBarStackPane.setVisible(true);
            botBarHBox.setVisible(true);
            controlBarsShown = true;
        }
    }

    private void toggleWordPanel() {
        if (wordPanelShown) {
            hideWordPanel();
        } else {
            showWordPanel();
        }
    }

    private void showWordPanel() {
        if (!wordPanelShown) {
            playerVBox.setPrefWidth(playerVBox.getWidth() - 500);
            playerVBox.setMaxWidth(playerVBox.getWidth() - 500);
            playerVBox.setMinWidth(playerVBox.getWidth() - 500);
            imageViewStackPane.setPrefWidth(imageViewStackPane.getWidth() - 500);
            imageViewStackPane.setMaxWidth(imageViewStackPane.getWidth() - 500);
            imageViewStackPane.setMinWidth(imageViewStackPane.getWidth() - 500);
            wordPanel.setPrefWidth(500);
            wordPanelShown = true;
        }
    }

    private void hideWordPanel() {
        if (wordPanelShown) {
            playerVBox.setPrefWidth(playerVBox.getWidth() + 500);
            playerVBox.setMaxWidth(playerVBox.getWidth() + 500);
            playerVBox.setMinWidth(playerVBox.getWidth() + 500);
            imageViewStackPane.setPrefWidth(imageViewStackPane.getWidth() + 500);
            imageViewStackPane.setMaxWidth(imageViewStackPane.getWidth() + 500);
            imageViewStackPane.setMinWidth(imageViewStackPane.getWidth() + 500);
            wordPanel.setPrefWidth(0);
            wordPanelShown = false;
        }
    }

    public void fill(VideoContentDetailsDto.VideoContentLocDto videoContentLocDto, SubtitleDto subtitleDto, Scene scene) {
        try {
            this.videoContentLocDto = videoContentLocDto;
            this.subtitleDto = subtitleDto;
            this.subtitle = linguaClient.getSubtitle(subtitleDto.getId().toString()).get();

            scene.setOnKeyPressed(keyEvent -> {
                switch (keyEvent.getCode()) {
                    case ESCAPE -> toggleControlBars();
                    case W -> toggleWordPanel();
                }
            });

            linguaClient.playVideoContent(embeddedMediaPlayer, videoContentLocDto.getId().toString());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private class MediaPlayerEventListener extends MediaPlayerEventAdapter {

        @Override
        public void paused(MediaPlayer mediaPlayer) {
            Platform.runLater(() -> {
                Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/play.png")));
                toggleImageView.setImage(image);
            });
        }

        @Override
        public void playing(MediaPlayer mediaPlayer) {
            Platform.runLater(() -> {
                Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/pause.png")));
                toggleImageView.setImage(image);
            });
        }

        @Override
        public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
            if (blockPositionChange.compareAndSet(false, true)) {
                Platform.runLater(() -> {
                    passedTimeLabel.setText(formatTime(newTime));
                    leftTimeLabel.setText(formatTime(mediaPlayer.media().info().duration() - newTime));
                    // show
                    if (subtitle.showNext(newTime)) {
                        showSubtitle();
                    }
                    // remove
                    if (currentSubtitleItem != null && currentSubtitleItem.getEndTime() < newTime) {
                        currentSubtitleItem = null;
                        textFlow1.getChildren().clear();
                        textFlow2.getChildren().clear();
                    }
                });
                blockPositionChange.set(false);
            }
        }

        private String formatTime(long time) {
            long ss = time / 1000;
            long mm = ss / 60;
            long hh = mm / 60;
            return String.format("%02d:%02d:%02d", hh, mm % 60, ss % 60);
        }

        private void showSubtitle() {
            currentSubtitleItem = subtitle.next();
            ObservableList<Node> children1 = textFlow1.getChildren();
            ObservableList<Node> children2 = textFlow2.getChildren();
            Collection<Text> textCollection1 = phraseToTextCollection(currentSubtitleItem.getFirstLangPhrase(), true);
            Collection<Text> textCollection2 = phraseToTextCollection(currentSubtitleItem.getSecondLangPhrase(), false);
            children1.clear();
            children2.clear();
            children1.addAll(textCollection1);
            children2.addAll(textCollection2);
        }

        private Collection<Text> phraseToTextCollection(String phrase, boolean interactive) {
            Deque<Text> textCollection = new ArrayDeque<>();
            String[] words = phrase.split(" ");
            if (words.length > 0) {
                for (String word : words) {
                    textCollection.add(strToText(word, interactive));
                    textCollection.add(strToText(" ", false));
                }
                textCollection.removeLast();
            }
            return textCollection;
        }

        private Text strToText(String s, boolean interactive) {
            Text text = new Text(s);
            text.setStyle("-fx-fill: red; -fx-font-size: 24");
            if (interactive) {
                text.setOnMouseClicked(mouseEvent -> dictionaryClient.lookup(s, videoContentLocDto.getLanguage().getTag(), subtitleDto.getSecondLanguage().getTag())
                        .thenAcceptAsync(dicResultDto -> Platform.runLater(() -> {
                            wordPanelController.fill(dicResultDto);
                            showWordPanel();
                        })));
            }
            return text;
        }

        @Override
        public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {
            if (!blockTimeSliderUpdate.get()) {
                Platform.runLater(() -> {
                    if (!timeSlider.isValueChanging()) {
                        setTimeSliderValueFromPosition(newPosition);
                    }
                });
            }
        }

        @Override
        public void muted(MediaPlayer mediaPlayer, boolean muted) {
            Platform.runLater(() -> {
                Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(muted ? "/img/mute.png" : "/img/sound.png")));
                soundImageView.setImage(image);
            });
        }
    }
}

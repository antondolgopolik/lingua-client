package by.bsuir.linguaclient.controller;

import by.bsuir.linguaclient.api.dictionary.DictionaryClient;
import by.bsuir.linguaclient.api.lingua.LinguaClient;
import by.bsuir.linguaclient.configuration.ResourceHolder;
import by.bsuir.linguaclient.controller.component.AbstractWordPanelController;
import by.bsuir.linguaclient.controller.component.WordPanelController;
import by.bsuir.linguaclient.controller.component.WordPanelWithChatController;
import by.bsuir.linguaclient.dto.lingua.LanguageDto;
import by.bsuir.linguaclient.dto.lingua.PersonalDuoWatchRequestDto;
import by.bsuir.linguaclient.dto.lingua.PlayerMessageDto;
import by.bsuir.linguaclient.dto.lingua.PlayerMessageType;
import by.bsuir.linguaclient.subtitle.Subtitle;
import by.bsuir.linguaclient.subtitle.SubtitleItem;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxControllerAndView;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurface;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import java.lang.reflect.Type;
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
    private StackPane rootStackPane;
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

    private AbstractWordPanelController wordPanelController;
    private Region wordPanel;

    private final FxWeaver fxWeaver;
    private final LinguaClient linguaClient;
    private final DictionaryClient dictionaryClient;
    private final ResourceHolder resourceHolder;

    private final MediaPlayerFactory mediaPlayerFactory;
    private final EmbeddedMediaPlayer embeddedMediaPlayer;

    private StompSession stompSession;
    private PersonalDuoWatchRequestDto duoWatchRequestDto;

    private LanguageDto videoContentLanguage;
    private LanguageDto secondLanguage;
    private Subtitle subtitle;
    private Scene scene;
    private EventHandler<? super KeyEvent> sceneKeyPressedHandler;


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

        menuButton.setOnAction(event -> {
            if (stompSession != null) {
                stompSession.disconnect();
            }
            mediaPlayerFactory.release();
            resourceHolder.setMediaPlayerFactory(null);
            embeddedMediaPlayer.controls().stop();
            embeddedMediaPlayer.release();
            resourceHolder.setEmbeddedMediaPlayer(null);
            scene.removeEventHandler(KeyEvent.KEY_PRESSED, sceneKeyPressedHandler);
            scene.setRoot(fxWeaver.loadView(CatalogController.class));
        });
        backButton.setOnAction(event -> {
            handleBack();

            PlayerMessageDto playerMessage = new PlayerMessageDto();
            playerMessage.setMessageType(PlayerMessageType.BACK);
            sendMessage(playerMessage);
        });
        toggleButton.setOnAction(event -> {
            handleToggle();

            PlayerMessageDto playerMessage = new PlayerMessageDto();
            playerMessage.setMessageType(PlayerMessageType.TOGGLE);
            sendMessage(playerMessage);
        });
        forwardButton.setOnAction(event -> {
            handleForward();

            PlayerMessageDto playerMessage = new PlayerMessageDto();
            playerMessage.setMessageType(PlayerMessageType.FORWARD);
            sendMessage(playerMessage);
        });
        timeSlider.valueChangingProperty().addListener((observableValue, oldV, newV) -> {
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

    private void handleBack() {
        if (blockPositionChange.compareAndSet(false, true)) {
            long newTime = embeddedMediaPlayer.status().time() - 10 * 1000;
            embeddedMediaPlayer.controls().setTime(newTime);
            textFlow1.getChildren().clear();
            textFlow2.getChildren().clear();
            subtitle.back(newTime);
            blockPositionChange.set(false);
        }
    }

    private void handleToggle() {
        embeddedMediaPlayer.controls().pause();
    }

    private void handleForward() {
        if (blockPositionChange.compareAndSet(false, true)) {
            long newTime = embeddedMediaPlayer.status().time() + 10 * 1000;
            embeddedMediaPlayer.controls().setTime(newTime);
            textFlow1.getChildren().clear();
            textFlow2.getChildren().clear();
            subtitle.forward(newTime);
            blockPositionChange.set(false);
        }
    }

    private void handleChangePosition(float position) {
        if (blockPositionChange.compareAndSet(false, true)) {
            embeddedMediaPlayer.controls().setPosition(position);
            textFlow1.getChildren().clear();
            textFlow2.getChildren().clear();
            subtitle.changePosition(embeddedMediaPlayer.status().time());
            blockPositionChange.set(false);
        }
    }

    private void setPositionFromTimeSliderValue(double sliderValue) {
        float position = (float) sliderValue / 100;
        handleChangePosition(position);

        PlayerMessageDto playerMessage = new PlayerMessageDto();
        playerMessage.setMessageType(PlayerMessageType.CHANGE_POSITION);
        playerMessage.setPayload(position);
        sendMessage(playerMessage);
    }

    private void setTimeSliderValueFromPosition(float position) {
        timeSlider.setValue(position * 100);
    }

    private void sendMessage(PlayerMessageDto playerMessage) {
        if (stompSession != null) {
            stompSession.send("/app/screening-room/" + duoWatchRequestDto.getId(), playerMessage);
        }
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
            wordPanel.setMaxWidth(500);
            wordPanel.setMaxWidth(500);
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
            wordPanel.setMaxWidth(0);
            wordPanel.setMaxWidth(0);
            wordPanelShown = false;
        }
    }

    public void fill(PersonalDuoWatchRequestDto duoWatchRequestDto,
                     UUID videoContentLocId,
                     UUID subtitleId,
                     LanguageDto videoContentLanguage,
                     LanguageDto secondLanguage,
                     Scene scene) {
        try {
            if (duoWatchRequestDto != null) {
                FxControllerAndView<WordPanelWithChatController, Region> controllerAndView = fxWeaver.load(WordPanelWithChatController.class);
                WordPanelWithChatController wordPanelWithChatController = controllerAndView.getController();
                wordPanelController = wordPanelWithChatController;
                wordPanel = controllerAndView.getView().orElseThrow();

                prepareForDuoWatch(duoWatchRequestDto);
                wordPanelWithChatController.fill(linguaClient.getUsername(), this::sendChatMessage);
            } else {
                FxControllerAndView<WordPanelController, Region> controllerAndView = fxWeaver.load(WordPanelController.class);
                wordPanelController = controllerAndView.getController();
                wordPanel = controllerAndView.getView().orElseThrow();
            }
            wordPanel.setPrefWidth(0);
            wordPanel.setMaxWidth(Region.USE_PREF_SIZE);
            wordPanel.setMinWidth(Region.USE_PREF_SIZE);
            rootStackPane.getChildren().add(wordPanel);
            StackPane.setAlignment(wordPanel, Pos.CENTER_RIGHT);

            this.videoContentLanguage = videoContentLanguage;
            this.secondLanguage = secondLanguage;
            this.subtitle = linguaClient.getSubtitle(subtitleId).get();

            this.scene = scene;
            this.sceneKeyPressedHandler = (EventHandler<KeyEvent>) keyEvent -> {
                switch (keyEvent.getCode()) {
                    case ESCAPE -> toggleControlBars();
                    case W -> toggleWordPanel();
                }
            };
            scene.setOnKeyPressed(sceneKeyPressedHandler);

            linguaClient.playVideoContent(embeddedMediaPlayer, videoContentLocId);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void prepareForDuoWatch(PersonalDuoWatchRequestDto duoWatchRequestDto) {
        this.duoWatchRequestDto = duoWatchRequestDto;
        this.stompSession = linguaClient.connectWebSocket();

        String playerStateDestination = "/private/reply/screening-room/" + duoWatchRequestDto.getId();
        stompSession.subscribe(playerStateDestination, new StompSessionHandlerAdapter() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return PlayerMessageDto.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                log.info("Received " + payload);
                PlayerMessageDto playerMessage = (PlayerMessageDto) payload;
                Platform.runLater(() -> {
                    switch (playerMessage.getMessageType()) {
                        case BACK -> handleBack();
                        case TOGGLE -> handleToggle();
                        case FORWARD -> handleForward();
                        case CHANGE_POSITION ->
                                handleChangePosition(((Double) playerMessage.getPayload()).floatValue());
                        case CHAT -> {
                            var wordPanelWitchChatController = (WordPanelWithChatController) wordPanelController;
                            var msg = (ArrayList<String>) playerMessage.getPayload();
                            wordPanelWitchChatController.showMessage(msg.get(0), msg.get(1), false);
                        }
                    }
                });
            }
        });
    }

    private void sendChatMessage(String message) {
        PlayerMessageDto playerMessage = new PlayerMessageDto();
        playerMessage.setMessageType(PlayerMessageType.CHAT);
        playerMessage.setPayload(new String[]{linguaClient.getUsername(), message});
        sendMessage(playerMessage);
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
            text.getStyleClass().add("subtitle-text");
            if (interactive) {
                text.setOnMouseClicked(mouseEvent -> dictionaryClient.lookup(s, videoContentLanguage.getTag(), secondLanguage.getTag())
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

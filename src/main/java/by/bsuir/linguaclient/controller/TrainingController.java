package by.bsuir.linguaclient.controller;

import by.bsuir.linguaclient.api.lingua.LinguaClient;
import by.bsuir.linguaclient.dto.lingua.DictionaryWordDto;
import by.bsuir.linguaclient.dto.lingua.TrainingAnswerDto;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxControllerAndView;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.*;

@Component
@FxmlView("/fxml/TrainingView.fxml")
@Slf4j
public class TrainingController implements Initializable {

    @FXML
    private Button backToDictionaryButton;
    @FXML
    private Label progressLabel;
    @FXML
    private Label mistakesLabel;
    @FXML
    private Label questionNumberLabel;
    @FXML
    private Label questionLabel;
    @FXML
    private TextField answerTextField;
    @FXML
    private Button checkButton;

    private final FxWeaver fxWeaver;
    private final LinguaClient linguaClient;

    private List<DictionaryWordDto> questions;
    private Iterator<DictionaryWordDto> questionIterator;
    private DictionaryWordDto currentQuestion;
    private int currentQuestionNumber;
    private int mistakes;

    private List<TrainingAnswerDto> trainingAnswerDtos;
    private List<TrainingResultController.TrainingResultItem> trainingResultItems;

    public TrainingController(FxWeaver fxWeaver, LinguaClient linguaClient) {
        this.fxWeaver = fxWeaver;
        this.linguaClient = linguaClient;
        this.trainingAnswerDtos = new ArrayList<>();
        this.trainingResultItems = new ArrayList<>();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        answerTextField.setOnAction(event -> {
            String answer = answerTextField.getText();
            if (currentQuestion.getFirstLanguageText().equalsIgnoreCase(answer)) {
                var trainingAnswerDto = new TrainingAnswerDto();
                trainingAnswerDto.setDictionaryWordId(currentQuestion.getId());
                trainingAnswerDto.setCorrect(true);
                trainingAnswerDtos.add(trainingAnswerDto);
                var trainingResultItem = new TrainingResultController.TrainingResultItem();
                trainingResultItem.setQuestion(currentQuestion.getSecondLanguageText());
                trainingResultItem.setAnswer(answer);
                trainingResultItem.setCorrectAnswer(currentQuestion.getFirstLanguageText());
                trainingResultItem.setTranscription(currentQuestion.getTranscription());
                trainingResultItem.setCorrect(true);
                trainingResultItems.add(trainingResultItem);
            } else {
                var trainingAnswerDto = new TrainingAnswerDto();
                trainingAnswerDto.setDictionaryWordId(currentQuestion.getId());
                trainingAnswerDto.setCorrect(false);
                trainingAnswerDtos.add(trainingAnswerDto);
                var trainingResultItem = new TrainingResultController.TrainingResultItem();
                trainingResultItem.setQuestion(currentQuestion.getSecondLanguageText());
                trainingResultItem.setAnswer(answer);
                trainingResultItem.setCorrectAnswer(currentQuestion.getFirstLanguageText());
                trainingResultItem.setTranscription(currentQuestion.getTranscription());
                trainingResultItem.setCorrect(false);
                trainingResultItems.add(trainingResultItem);

                mistakes++;
                mistakesLabel.setText("Mistakes: " + mistakes);
            }
            answerTextField.clear();
            if (!nextQuestion()) {
                FxControllerAndView<TrainingResultController, Parent> controllerAndView = fxWeaver.load(TrainingResultController.class);
                controllerAndView.getController().fill(trainingResultItems);
                answerTextField.getScene().setRoot(controllerAndView.getView().orElseThrow());
            }
        });
    }

    public void fill(List<DictionaryWordDto> questions) {
        this.questions = questions;
        this.questionIterator = questions.iterator();
        nextQuestion();
    }

    private boolean nextQuestion() {
        if (questionIterator.hasNext()) {
            progressLabel.setText("Progress: " + currentQuestionNumber + "/" + questions.size());
            currentQuestion = questionIterator.next();
            currentQuestionNumber++;
            questionNumberLabel.setText("Question " + currentQuestionNumber);
            questionLabel.setText(currentQuestion.getSecondLanguageText());
            return true;
        }
        return false;
    }
}

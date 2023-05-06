package by.bsuir.linguaclient.dto.dictionary;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DefinitionDto {

    @JsonProperty("text")
    private String text;
    @JsonProperty("ts")
    private String transcription;
    @JsonProperty("pos")
    private String partOfSpeech;
    @JsonProperty("tr")
    private List<TranslationDto> translations;
}

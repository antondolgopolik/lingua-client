package by.bsuir.linguaclient.dto.dictionary;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TranslationDto {

    @JsonProperty("text")
    private String text;
    @JsonProperty("pos")
    private String partOfSpeech;
    @JsonProperty("syn")
    private List<SynonymDto> synonyms;
    @JsonProperty("mean")
    private List<MeanDto> means;
    @JsonProperty("ex")
    private List<ExampleDto> examples;
}

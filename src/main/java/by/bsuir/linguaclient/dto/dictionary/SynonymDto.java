package by.bsuir.linguaclient.dto.dictionary;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SynonymDto {

    @JsonProperty("text")
    private String text;
    @JsonProperty("pos")
    private String partOfSpeech;
}

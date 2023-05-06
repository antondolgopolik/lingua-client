package by.bsuir.linguaclient.dto.dictionary;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ExampleDto {

    @JsonProperty("text")
    private String text;
    @JsonProperty("tr")
    private List<TranslationDto> translations;
}

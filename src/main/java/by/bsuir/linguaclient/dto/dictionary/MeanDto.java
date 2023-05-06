package by.bsuir.linguaclient.dto.dictionary;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MeanDto {

    @JsonProperty("text")
    private String text;
}

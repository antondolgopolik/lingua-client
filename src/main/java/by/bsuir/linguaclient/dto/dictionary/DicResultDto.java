package by.bsuir.linguaclient.dto.dictionary;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DicResultDto {

    @JsonProperty("def")
    List<DefinitionDto> definitions;
}

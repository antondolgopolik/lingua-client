package by.bsuir.linguaclient.dto.lingua;

import lombok.Data;

@Data
public class DictionaryWordDto {
    private Long id;
    private String firstLanguageText;
    private String secondLanguageText;
    private String transcription;
    private Integer mastery;
}

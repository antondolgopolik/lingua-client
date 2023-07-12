package by.bsuir.linguaclient.dto.lingua;

import lombok.Data;

@Data
public class AddWordToDictionaryFormDto {
    private String firstLanguageText;
    private String secondLanguageText;
    private String transcription;
}

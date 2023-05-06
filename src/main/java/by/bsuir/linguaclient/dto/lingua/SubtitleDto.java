package by.bsuir.linguaclient.dto.lingua;

import lombok.Data;

import java.util.UUID;

@Data
public class SubtitleDto {
    private UUID id;
    private LanguageDto secondLanguage;

    @Override
    public String toString() {
        return secondLanguage.getName();
    }
}

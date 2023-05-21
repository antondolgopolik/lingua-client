package by.bsuir.linguaclient.dto.lingua;

import lombok.Data;

@Data
public class DictionaryDto {
    private Long id;
    private String name;
    private LanguageDto firstLanguage;
    private LanguageDto secondLanguage;
    private Integer size;
}

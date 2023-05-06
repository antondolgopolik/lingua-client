package by.bsuir.linguaclient.dto.lingua;

import lombok.Data;

@Data
public class LanguageDto {
    private Long id;
    private String name;
    private String tag;

    @Override
    public String toString() {
        return name;
    }
}

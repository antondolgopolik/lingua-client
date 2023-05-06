package by.bsuir.linguaclient.dto.lingua;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Data
public class DuoWatchRequestCatalogItemDto {
    private Long id;
    private VideoContentLocDto videoContentLocDto;
    private LanguageDto secondLanguage;

    @Data
    public static class VideoContentLocDto {
        private UUID id;
        private CatalogItemDto catalogItemDto;
        private LanguageDto language;
    }
}

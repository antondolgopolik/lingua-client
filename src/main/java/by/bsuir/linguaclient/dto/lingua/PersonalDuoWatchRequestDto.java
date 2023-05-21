package by.bsuir.linguaclient.dto.lingua;

import lombok.Data;

import java.util.UUID;

@Data
public class PersonalDuoWatchRequestDto {
    private Long id;
    private VideoContentLocDto videoContentLocDto;
    private LanguageDto secondLanguage;
    private DuoWatchRequestStatus status;
    private String partnerTgUsername;
    private Boolean relevanceConfirmationRequired;

    @Data
    public static class VideoContentLocDto {
        private UUID id;
        private CatalogItemDto catalogItemDto;
        private LanguageDto language;
    }
}

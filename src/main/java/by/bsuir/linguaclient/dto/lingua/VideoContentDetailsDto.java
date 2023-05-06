package by.bsuir.linguaclient.dto.lingua;

import lombok.Data;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Data
public class VideoContentDetailsDto {
    private UUID id;
    private String name;
    private String description;
    private Integer duration;
    private Long views;
    private List<GenreDto> genres;
    private List<VideoContentLocDto> videoContentLocDtos;

    @Data
    public static class VideoContentLocDto {
        private UUID id;
        private LanguageDto language;
        private List<SubtitleDto> subtitles;

        @Override
        public String toString() {
            return language.getName();
        }
    }
}

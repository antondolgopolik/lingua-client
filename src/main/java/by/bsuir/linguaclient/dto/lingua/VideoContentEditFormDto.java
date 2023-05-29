package by.bsuir.linguaclient.dto.lingua;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class VideoContentEditFormDto {
    private UUID id;
    private String name;
    private String shortDescription;
    private String description;
    private Integer duration;
    private List<GenreDto> genres;
}

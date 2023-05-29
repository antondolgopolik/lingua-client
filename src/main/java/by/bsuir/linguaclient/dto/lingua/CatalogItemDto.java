package by.bsuir.linguaclient.dto.lingua;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CatalogItemDto {
    private UUID id;
    private String name;
    private String shortDescription;
    private Integer duration;
    private Long views;
    private List<GenreDto> genres;
}

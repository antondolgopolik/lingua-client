package by.bsuir.linguaclient.dto.lingua;

import lombok.Data;

import java.util.List;

@Data
public class CatalogItemDto {
    private String id;
    private String name;
    private Integer duration;
    private Long views;
    private List<GenreDto> genres;
}

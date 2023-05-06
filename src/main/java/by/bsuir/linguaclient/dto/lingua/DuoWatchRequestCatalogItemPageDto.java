package by.bsuir.linguaclient.dto.lingua;

import lombok.Data;

import java.util.List;

@Data
public class DuoWatchRequestCatalogItemPageDto {
    private Integer totalPages;
    private List<DuoWatchRequestCatalogItemDto> duoWatchRequestCatalogItemDtos;
}
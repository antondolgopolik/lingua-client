package by.bsuir.linguaclient.dto.lingua;

import lombok.Data;

import java.util.List;

@Data
public class PersonalDuoWatchRequestPageDto {
    private Integer totalPages;
    private List<PersonalDuoWatchRequestDto> personalDuoWatchRequestDtos;
}

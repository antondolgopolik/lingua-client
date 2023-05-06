package by.bsuir.linguaclient.dto.lingua;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateDuoWatchRequestFormDto {
    private UUID videoContentLocId;
    private Long secondLangId;
}

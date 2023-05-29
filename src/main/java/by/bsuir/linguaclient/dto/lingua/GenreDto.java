package by.bsuir.linguaclient.dto.lingua;

import lombok.Data;

@Data
public class GenreDto {
    private Long id;
    private String name;

    @Override
    public String toString() {
        return name;
    }
}

package by.bsuir.linguaclient.dto.lingua;

import lombok.Data;

@Data
public class PlayerMessageDto {
    private PlayerMessageType messageType;
    private Object payload;
}

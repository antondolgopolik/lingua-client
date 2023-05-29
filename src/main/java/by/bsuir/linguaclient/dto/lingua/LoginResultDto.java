package by.bsuir.linguaclient.dto.lingua;

import lombok.Data;

import java.util.List;

@Data
public class LoginResultDto {
    private String username;
    private List<Role> roles;
    private String token;
}

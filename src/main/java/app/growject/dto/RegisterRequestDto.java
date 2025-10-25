package app.growject.dto;

import lombok.Data;
import java.util.List;

@Data // Lombok pour Getters/Setters/ToString/EqualsAndHashCode
public class RegisterRequestDto {
    private String name;
    private String email;
    private String password;
    private String bio;
    private List<String> skills;
}
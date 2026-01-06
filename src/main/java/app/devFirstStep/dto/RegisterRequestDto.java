package app.devFirstStep.dto;

import lombok.*;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RegisterRequestDto {
    private String name;
    private String email;
    private String password;
    private String bio;
    private List<String> skills;
}
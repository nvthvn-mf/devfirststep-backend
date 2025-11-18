package app.devFirstStep.dto;

import lombok.*;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RegisterRequestDto {
    private String name;
    private String email;
    private String password;
}
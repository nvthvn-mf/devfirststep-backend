package app.devFirstStep.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UserResponseDto {
    private Long id;
    private String name;
    private String tag;
    private String email;
    private String bio;
    private String avatarUrl;
    private List<String> skills;
    private String level;
}

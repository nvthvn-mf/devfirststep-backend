package app.devFirstStep.dto;

import app.devFirstStep.entity.DeveloperLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ProfileUpdateRequestDto {

    private String name;
    private String tag;
    private String bio;
    private String avatarUrl;
    private List<String> skills;
    private DeveloperLevel level; // On accepte l'Enum en entrée
}

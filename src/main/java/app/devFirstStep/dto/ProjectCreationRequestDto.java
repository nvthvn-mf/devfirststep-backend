package app.devFirstStep.dto;

import app.devFirstStep.entity.ProjectStatus;
import lombok.Data;

import java.util.List;

@Data
public class ProjectCreationRequestDto {
    private String name;
    private String description;
    private String objectives;
    private List<String> stacks;
    private ProjectStatus status;
}

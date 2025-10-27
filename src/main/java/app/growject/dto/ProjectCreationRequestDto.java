package app.growject.dto;

import app.growject.entity.ProjectStatus;
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

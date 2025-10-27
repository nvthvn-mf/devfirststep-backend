package app.growject.dto;

import app.growject.entity.ProjectStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProjectResponseDto {
    private Long id;
    private String name;
    private String description;
    private String objectives;
    private List<String> stacks;
    private ProjectStatus status;
    private LocalDateTime createdAt;

    // Pour indiquer qui est le propriétaire sans exposer tous ses détails
    private UserResponseDto owner;

    // FUTURE: Nombre de tâches terminées / total pour le dashboard
}

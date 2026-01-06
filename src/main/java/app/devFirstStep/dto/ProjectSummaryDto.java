package app.devFirstStep.dto;

import app.devFirstStep.entity.ProjectStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProjectSummaryDto {
    private Long id;
    private String name;
    private String description;
    private ProjectStatus status;
    private LocalDateTime createdAt;
    private String ownerName;
    // FUTURE: Indicateurs de progression rapides
}

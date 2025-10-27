package app.growject.dto;

import app.growject.entity.ProjectStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProjectSummaryDto {
    private Long id;
    private String name;
    private ProjectStatus status;
    private LocalDateTime createdAt;
    private String ownerName;
    // FUTURE: Indicateurs de progression rapides
}

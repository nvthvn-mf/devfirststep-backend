package app.growject.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectStatsDto {
    private Long projectId;
    private int totalTasks;
    private int todoCount;
    private int inProgressCount;
    private int doneCount;
    private double completionPercentage; // 0.0 à 100.0
}
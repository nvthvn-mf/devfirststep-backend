package app.growject.dto;

import app.growject.entity.TaskStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponseDto {
    private Long id;
    private Long projectId; // Lien explicite au projet
    private String title;
    private String description;
    private TaskStatus status;
    private Integer orderIndex;
    private LocalDateTime createdAt;

    // Informations minimales sur l'utilisateur assigné (si attribué)
    private Long assignedUserId;
    private String assignedUserName;
}

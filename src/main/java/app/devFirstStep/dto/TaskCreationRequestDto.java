package app.devFirstStep.dto;

import app.devFirstStep.entity.TaskStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCreationRequestDto {
    private String title;
    private String description;
    private TaskStatus status;

    // L'index pour l'ordre dans la colonne (crucial pour le drag & drop)
    private Integer orderIndex;

    // Pour l'attribution : dans le MVP, on s'attribue à soi-même.
    // Pour l'évolutivité, on garde un champ pour l'ID de l'utilisateur assigné (optionnel).
    private Long assignedUserId;
}

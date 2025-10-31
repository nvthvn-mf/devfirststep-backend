package app.growject.service;

import app.growject.dto.ProjectStatsDto;
import app.growject.entity.Project;
import app.growject.entity.Task;
import app.growject.entity.TaskStatus;
import app.growject.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    // Dépendances nécessaires : pour la récupération des tâches et la vérification de la propriété du projet
    private final TaskRepository taskRepository;
    private final ProjectService projectService;

    /**
     * Calcule les statistiques de progression pour un projet donné.
     * Assure la sécurité : seul le propriétaire peut voir les statistiques.
     */
    @Transactional(readOnly = true)
    public ProjectStatsDto getProjectStats(Long projectId, String userEmail) {

        // 1. Vérification de la propriété du projet (sécurité)
        // projectService.getOwnedProject(projectId, userEmail) lève une exception si l'accès est refusé ou si le projet n'existe pas.
        Project project = projectService.getOwnedProject(projectId, userEmail);

        // 2. Récupération de toutes les tâches liées à ce projet
        var tasks = taskRepository.findByProjectId(projectId);

        int totalTasks = tasks.size();

        // 3. Compter les tâches par statut en utilisant le Stream API
        Map<TaskStatus, Long> counts = tasks.stream()
                // Groupement par statut de tâche et comptage
                .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));

        // Récupération sécurisée des compteurs (retourne 0 si aucune tâche dans ce statut)
        int todoCount = counts.getOrDefault(TaskStatus.TO_DO, 0L).intValue();
        int inProgressCount = counts.getOrDefault(TaskStatus.IN_PROGRESS, 0L).intValue();
        int doneCount = counts.getOrDefault(TaskStatus.DONE, 0L).intValue();

        // 4. Calcul du pourcentage de complétion
        double completionPercentage = 0.0;
        if (totalTasks > 0) {
            completionPercentage = (double) doneCount / totalTasks * 100.0;
        }

        // 5. Construction du DTO de réponse
        return ProjectStatsDto.builder()
                .projectId(projectId)
                .totalTasks(totalTasks)
                .todoCount(todoCount)
                .inProgressCount(inProgressCount)
                .doneCount(doneCount)
                // Arrondi à deux décimales pour l'affichage frontend
                .completionPercentage(Math.round(completionPercentage * 100.0) / 100.0)
                .build();
    }
}

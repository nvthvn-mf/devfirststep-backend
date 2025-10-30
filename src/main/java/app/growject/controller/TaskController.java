package app.growject.controller;

import app.growject.dto.TaskCreationRequestDto;
import app.growject.dto.TaskResponseDto;
import app.growject.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * Récupère toutes les tâches d'un projet (le tableau Kanban).
     */
    @GetMapping
    public ResponseEntity<List<TaskResponseDto>> getProjectTasks(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<TaskResponseDto> tasks = taskService.getTasksByProject(projectId, userDetails.getUsername());
        return ResponseEntity.ok(tasks);
    }

    /**
     * Crée une nouvelle tâche dans le projet spécifié.
     */
    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(
            @PathVariable Long projectId,
            @RequestBody TaskCreationRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails) {

        TaskResponseDto response = taskService.createTask(projectId, userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Met à jour une tâche spécifique (contenu ou changement de colonne/ordre).
     */
    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponseDto> updateTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestBody TaskCreationRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails) {

        TaskResponseDto response = taskService.updateTask(projectId, taskId, userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Supprime une tâche spécifique.
     */
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserDetails userDetails) {

        taskService.deleteTask(projectId, taskId, userDetails.getUsername());
        return ResponseEntity.noContent().build(); // 204 No Content, standard REST pour la suppression
    }
}

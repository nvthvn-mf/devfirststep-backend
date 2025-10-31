package app.devFirstStep.service;

import app.devFirstStep.dto.TaskCreationRequestDto;
import app.devFirstStep.dto.TaskResponseDto;
import app.devFirstStep.entity.Project;
import app.devFirstStep.entity.Task;
import app.devFirstStep.entity.User;
import app.devFirstStep.repository.TaskRepository;
import app.devFirstStep.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final UserRepository userRepository;

    /**
     * Helper pour trouver l'utilisateur par email.
     */
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé."));
    }

    /**
     * Helper pour vérifier que l'utilisateur est bien le propriétaire du projet.
     * Lance une exception si le projet n'existe pas ou si l'utilisateur n'est pas le propriétaire.
     */
    private Project getOwnedProject(Long projectId, String userEmail) {
        Project project = projectService.getProjectEntityById(projectId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Projet non trouvé."));

        User owner = findUserByEmail(userEmail);

        if (!project.getOwner().equals(owner)) {
            throw new AccessDeniedException("Accès refusé : vous n'êtes pas le propriétaire de ce projet.");
        }
        return project;
    }

    /**
     * Crée une nouvelle tâche dans un projet spécifié.
     */
    @Transactional
    public TaskResponseDto createTask(Long projectId, String userEmail, TaskCreationRequestDto request) {
        // 1. Vérification des droits et récupération du projet
        Project project = getOwnedProject(projectId, userEmail);
        User assignedTo = null;

        // 2. Si un utilisateur est assigné, on le récupère
        if (request.getAssignedUserId() != null) {
            assignedTo = userRepository.findById(request.getAssignedUserId())
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Utilisateur assigné non trouvé."));
        } else {
            // Par défaut, s'auto-assigner (le propriétaire)
            assignedTo = project.getOwner();
        }

        // 3. Mapper et sauvegarder la tâche
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : app.devFirstStep.entity.TaskStatus.TO_DO)
                .orderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0)
                .project(project)
                .assignedTo(assignedTo)
                .build();

        Task savedTask = taskRepository.save(task);
        return mapToTaskResponseDto(savedTask);
    }

    /**
     * Récupère toutes les tâches pour un projet (le Kanban complet).
     */
    @Transactional(readOnly = true)
    public List<TaskResponseDto> getTasksByProject(Long projectId, String userEmail) {
        // La vérification des droits est faite implicitement par getOwnedProject.
        // On vérifie que l'utilisateur est propriétaire avant de récupérer.
        getOwnedProject(projectId, userEmail);

        List<Task> tasks = taskRepository.findByProjectId(projectId);

        // Trier les tâches par index pour assurer l'ordre Kanban dans le frontend
        return tasks.stream()
                .sorted(Comparator.comparing(Task::getOrderIndex))
                .map(this::mapToTaskResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Met à jour une tâche spécifique. Utilisé pour le contenu ou le Drag & Drop.
     */
    @Transactional
    public TaskResponseDto updateTask(Long projectId, Long taskId, String userEmail, TaskCreationRequestDto request) {
        // 1. Vérifier la propriété du projet
        getOwnedProject(projectId, userEmail);

        // 2. Récupérer la tâche et s'assurer qu'elle appartient au bon projet
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tâche non trouvée."));

        if (!task.getProject().getId().equals(projectId)) {
            throw new ResponseStatusException(NOT_FOUND, "La tâche n'appartient pas à ce projet.");
        }

        // 3. Mise à jour des champs
        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getOrderIndex() != null) task.setOrderIndex(request.getOrderIndex());

        // La gestion de l'utilisateur assigné est plus complexe et peut être simplifiée ici
        if (request.getAssignedUserId() != null) {
            User assignedTo = userRepository.findById(request.getAssignedUserId())
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Utilisateur assigné non trouvé."));
            task.setAssignedTo(assignedTo);
        }

        // 4. Sauvegarde
        Task updatedTask = taskRepository.save(task);
        return mapToTaskResponseDto(updatedTask);
    }

    /**
     * Supprime une tâche.
     */
    @Transactional
    public void deleteTask(Long projectId, Long taskId, String userEmail) {
        // 1. Vérifier la propriété du projet
        getOwnedProject(projectId, userEmail);

        // 2. Récupérer la tâche et s'assurer qu'elle appartient au bon projet
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Tâche non trouvée."));

        if (!task.getProject().getId().equals(projectId)) {
            throw new ResponseStatusException(NOT_FOUND, "La tâche n'appartient pas à ce projet.");
        }

        // 3. Suppression
        taskRepository.delete(task);
    }

    // --- MAPPERS ---

    private TaskResponseDto mapToTaskResponseDto(Task task) {
        // Récupère l'ID et le nom de l'utilisateur assigné
        Long assignedUserId = task.getAssignedTo() != null ? task.getAssignedTo().getId() : null;
        String assignedUserName = task.getAssignedTo() != null ? task.getAssignedTo().getName() : null;

        return TaskResponseDto.builder()
                .id(task.getId())
                .projectId(task.getProject().getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .orderIndex(task.getOrderIndex())
                .createdAt(task.getCreatedAt())
                .assignedUserId(assignedUserId)
                .assignedUserName(assignedUserName)
                .build();
    }
}

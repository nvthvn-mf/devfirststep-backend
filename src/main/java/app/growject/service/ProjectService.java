package app.growject.service;

import app.growject.dto.ProjectCreationRequestDto;
import app.growject.dto.ProjectResponseDto;
import app.growject.dto.ProjectSummaryDto;
import app.growject.dto.UserResponseDto;
import app.growject.entity.Project;
import app.growject.entity.ProjectStatus;
import app.growject.entity.User;
import app.growject.repository.ProjectRepository;
import app.growject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ProjectService {


    private final ProjectRepository projectRepository;
    private final UserRepository userRepository; // Pour récupérer l'Owner

    // Note : On pourrait injecter le UserService si on voulait réutiliser
    // la logique de mapping User -> UserResponseDto, mais le faire ici est simple pour le MVP.

    // Récupère l'utilisateur à partir de l'email pour l'associer au projet
    public User findUserByEmail(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + userEmail));
    }

    /**
     * Crée un nouveau projet pour l'utilisateur authentifié.
     */
    @Transactional
    public ProjectResponseDto createProject(String userEmail, ProjectCreationRequestDto request) {
        User owner = findUserByEmail(userEmail);

        // 1. Mapper le DTO vers l'Entité
        Project newProject = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .objectives(request.getObjectives())
                .stacks(request.getStacks())
                .owner(owner) // L'utilisateur authentifié est le propriétaire
                .status(ProjectStatus.ACTIVE) // Passe en actif dès la création
                .build();

        // 2. Sauvegarder
        Project savedProject = projectRepository.save(newProject);

        // 3. Mapper et retourner la réponse détaillée
        return mapToProjectResponseDto(savedProject);
    }
    /**
     * Met à jour les informations d'un projet existant.
     */
    @Transactional
    public ProjectResponseDto updateProject(String userEmail, ProjectCreationRequestDto request, Long projectId) {
        User owner = findUserByEmail(userEmail);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Projet non trouvé."));

        // Vérification de la propriété
        if (!project.getOwner().equals(owner)) {
            throw new AccessDeniedException("Accès refusé : ce projet ne vous appartient pas.");
        }

        // Mise à jour des champs
        if (request.getName() != null) project.setName(request.getName());
        if (request.getDescription() != null) project.setDescription(request.getDescription());
        if (request.getObjectives() != null) project.setObjectives(request.getObjectives());
        if (request.getStacks() != null) project.setStacks(request.getStacks());
        if (request.getStatus() != null) project.setStatus(request.getStatus());

        Project savedProject = projectRepository.save(project);
        return mapToProjectResponseDto(savedProject);
    }
    @Transactional
    public void deleteProject(String userEmail, Long projectId) {
        User owner = findUserByEmail(userEmail);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

        if (!project.getOwner().equals(owner)) {
            throw new ResponseStatusException(NOT_FOUND,"Accès refusé : ce projet ne vous appartient pas");
        }

        projectRepository.delete(project);
    }

    /**
     * Récupère la liste des projets pour l'utilisateur authentifié.
     */
    @Transactional(readOnly = true)
    public List<ProjectSummaryDto> getUserProjects(String userEmail) {
        User owner = findUserByEmail(userEmail);

        List<Project> projects = projectRepository.findByOwnerId(owner.getId());

        // Mappage vers une liste de ProjectSummaryDto
        return projects.stream()
                .map(this::mapToProjectSummaryDto)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les détails d'un projet par son ID.
     */
    @Transactional(readOnly = true)
    public ProjectResponseDto getProjectDetails(Long projectId, String userEmail) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé : " + projectId));

        // Vérification de propriété : seulement le propriétaire peut le voir pour le MVP
        if (!project.getOwner().getEmail().equals(userEmail)) {
            throw new SecurityException("Accès refusé : L'utilisateur n'est pas le propriétaire de ce projet.");
        }

        return mapToProjectResponseDto(project);
    }
    public Optional<Project> getProjectEntityById(Long projectId) {
        return projectRepository.findById(projectId);
    }

    // --- MAPPERS ---

    private ProjectResponseDto mapToProjectResponseDto(Project project) {
        return ProjectResponseDto.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .objectives(project.getObjectives())
                .stacks(project.getStacks())
                .status(project.getStatus())
                .createdAt(project.getCreatedAt())
                .owner(UserResponseDto.builder() // Mapping partiel de l'Owner
                        .id(project.getOwner().getId())
                        .name(project.getOwner().getName())
                        .email(project.getOwner().getEmail())
                        .build())
                .build();
    }

    private ProjectSummaryDto mapToProjectSummaryDto(Project project) {
        return ProjectSummaryDto.builder()
                .id(project.getId())
                .name(project.getName())
                .status(project.getStatus())
                .createdAt(project.getCreatedAt())
                .ownerName(project.getOwner().getName())
                .build();
    }

}

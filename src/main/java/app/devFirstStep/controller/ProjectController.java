package app.devFirstStep.controller;

import app.devFirstStep.dto.ProjectCreationRequestDto;
import app.devFirstStep.dto.ProjectResponseDto;
import app.devFirstStep.dto.ProjectSummaryDto;
import app.devFirstStep.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // POST /api/v1/projects : Crée un nouveau projet
    @PostMapping
    public ResponseEntity<ProjectResponseDto> createProject(
            @RequestBody ProjectCreationRequestDto request,
            // Récupère l'utilisateur authentifié directement via Spring Security
            @AuthenticationPrincipal UserDetails userDetails) {

        ProjectResponseDto response = projectService.createProject(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    // GET /api/v1/projects : Liste les projets de l'utilisateur
    @GetMapping
    public ResponseEntity<List<ProjectSummaryDto>> getUserProjects(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<ProjectSummaryDto> projects = projectService.getUserProjects(userDetails.getUsername());
        return ResponseEntity.ok(projects);
    }

    // GET /api/v1/projects/{id} : Détails d'un projet
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> getProjectDetails(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        ProjectResponseDto project = projectService.getProjectDetails(id, userDetails.getUsername());
        return ResponseEntity.ok(project);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> updateProject(
            @PathVariable Long id,
            @RequestBody ProjectCreationRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails){
        ProjectResponseDto project = projectService.updateProject(userDetails.getUsername(), request,id);
        return ResponseEntity.ok(project);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        projectService.deleteProject(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}

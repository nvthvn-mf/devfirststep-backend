package app.growject.controller;

import app.growject.dto.ProjectStatsDto;
import app.growject.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard") // Chemin logique pour les données du tableau de bord
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Récupère les statistiques détaillées pour un projet spécifique.
     * Chemin : /api/v1/dashboard/projects/{projectId}/stats
     * Nécessite une authentification (JWT).
     */
    @GetMapping("/projects/{projectId}/stats")
    public ResponseEntity<ProjectStatsDto> getProjectStats(
            @PathVariable Long projectId,
            // Récupère l'utilisateur authentifié (son email = username)
            @AuthenticationPrincipal UserDetails userDetails) {

        ProjectStatsDto stats = dashboardService.getProjectStats(
                projectId,
                userDetails.getUsername()
        );
        return ResponseEntity.ok(stats);
    }
}

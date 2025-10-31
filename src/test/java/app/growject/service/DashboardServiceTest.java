package app.growject.service;

import app.growject.dto.ProjectStatsDto;
import app.growject.entity.*;
import app.growject.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.when;

/**
 * Tests Unitaires pour DashboardService.
 * Valide les calculs de statistiques et la sécurité.
 */
@ExtendWith(MockitoExtension.class)
public class DashboardServiceTest {

    private static final String TEST_EMAIL = "stats.user@growject.com";
    private static final Long PROJECT_ID = 1L;
    private static final String OTHER_EMAIL = "other.user@growject.com";

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private ProjectService projectService; // Pour la vérification de propriété

    @InjectMocks
    private DashboardService dashboardService;

    private User mockOwner;
    private Project mockProject;

    @BeforeEach
    void setUp() {
        mockOwner = User.builder().email(TEST_EMAIL).name("Stats Owner").build();

        mockProject = Project.builder()
                .id(PROJECT_ID)
                .name("Test Project")
                .owner(mockOwner)
                .createdAt(LocalDateTime.now())
                .status(ProjectStatus.ACTIVE)
                .build();
    }

    /**
     * Crée une liste de tâches pour simuler différents statuts.
     */
    private List<Task> createMockTasks(int todo, int progress, int done) {
        List<Task> tasks = new java.util.ArrayList<>();

        // Tâches TO_DO
        for (int i = 0; i < todo; i++) {
            tasks.add(Task.builder().status(TaskStatus.TO_DO).project(mockProject).build());
        }
        // Tâches IN_PROGRESS
        for (int i = 0; i < progress; i++) {
            tasks.add(Task.builder().status(TaskStatus.IN_PROGRESS).project(mockProject).build());
        }
        // Tâches DONE
        for (int i = 0; i < done; i++) {
            tasks.add(Task.builder().status(TaskStatus.DONE).project(mockProject).build());
        }
        return tasks;
    }

    @Test
    void testGetProjectStatsSuccess() {
        // GIVEN: 2 TO_DO, 3 IN_PROGRESS, 5 DONE (Total 10)
        List<Task> tasks = createMockTasks(2, 3, 5);

        // 1. Mock de la vérification de propriété
        when(projectService.getOwnedProject(PROJECT_ID, TEST_EMAIL)).thenReturn(mockProject);
        // 2. Mock de la récupération des tâches
        when(taskRepository.findByProjectId(PROJECT_ID)).thenReturn(tasks);

        // WHEN
        ProjectStatsDto stats = dashboardService.getProjectStats(PROJECT_ID, TEST_EMAIL);

        // THEN: Vérification des compteurs
        assertNotNull(stats);
        assertEquals(PROJECT_ID, stats.getProjectId());
        assertEquals(10, stats.getTotalTasks());
        assertEquals(2, stats.getTodoCount());
        assertEquals(3, stats.getInProgressCount());
        assertEquals(5, stats.getDoneCount());

        // Vérification du pourcentage (5/10 * 100 = 50.0)
        assertEquals(50.0, stats.getCompletionPercentage());
    }

    @Test
    void testGetProjectStatsSuccess_ZeroTasks() {
        // GIVEN: 0 tâches
        List<Task> tasks = createMockTasks(0, 0, 0);

        when(projectService.getOwnedProject(PROJECT_ID, TEST_EMAIL)).thenReturn(mockProject);
        when(taskRepository.findByProjectId(PROJECT_ID)).thenReturn(tasks);

        // WHEN
        ProjectStatsDto stats = dashboardService.getProjectStats(PROJECT_ID, TEST_EMAIL);

        // THEN
        assertEquals(0, stats.getTotalTasks());
        assertEquals(0, stats.getDoneCount());
        // Le pourcentage doit être 0.0, pas NaN
        assertEquals(0.0, stats.getCompletionPercentage());
    }

    @Test
    void testGetProjectStatsThrowsAccessDenied() {
        // GIVEN: La vérification de propriété échoue
        when(projectService.getOwnedProject(PROJECT_ID, OTHER_EMAIL))
                .thenThrow(new AccessDeniedException("Accès refusé."));

        // WHEN & THEN: L'exception doit être propagée
        assertThrows(AccessDeniedException.class,
                () -> dashboardService.getProjectStats(PROJECT_ID, OTHER_EMAIL));
    }
}

package app.growject.service;

import app.growject.dto.ProjectCreationRequestDto;
import app.growject.dto.ProjectResponseDto;
import app.growject.entity.DeveloperLevel;
import app.growject.entity.Project;
import app.growject.entity.ProjectStatus;
import app.growject.entity.User;
import app.growject.repository.ProjectRepository;
import app.growject.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests Unitaires pour ProjectService.
 */
@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    private static final String TEST_EMAIL = "test.owner@growject.com";
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_PROJECT_ID = 10L;

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectService projectService;

    private User mockOwner;
    private ProjectCreationRequestDto creationRequest;
    private Project mockProject;

    @BeforeEach
    void setUp() {
        // Mock de l'utilisateur propriétaire (commun à tous les tests)
        mockOwner = User.builder()
                .id(TEST_USER_ID)
                .email(TEST_EMAIL)
                .name("Test Owner")
                .password("hashed_pwd")
                .level(DeveloperLevel.EXPERT)
                .build();

        // Mock de la requête de création (commun à tous les tests)
        creationRequest = new ProjectCreationRequestDto();
        creationRequest.setName("Kanban Board App");
        creationRequest.setDescription("A simple Kanban.");
        creationRequest.setStacks(List.of("React", "Spring Boot"));

        // Mock du projet sauvegardé (commun à tous les tests)
        mockProject = Project.builder()
                .id(TEST_PROJECT_ID)
                .name(creationRequest.getName())
                .description(creationRequest.getDescription())
                .stacks(creationRequest.getStacks())
                .owner(mockOwner)
                .status(ProjectStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        // NOTE: Le mock par défaut when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(...)
        // a été retiré de setUp() et est placé dans les méthodes de test qui en ont besoin.
    }

    @Test
    void testCreateProjectSuccess() {
        // [CONFIG SPÉCIFIQUE AU TEST] : L'utilisateur existe et la sauvegarde réussit.
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(mockOwner));
        when(projectRepository.save(any(Project.class))).thenReturn(mockProject);

        // Exécution
        ProjectResponseDto response = projectService.createProject(TEST_EMAIL, creationRequest);

        // Vérifications
        assertNotNull(response);
        assertEquals(TEST_PROJECT_ID, response.getId());
        assertEquals(TEST_EMAIL, response.getOwner().getEmail());
        assertEquals(ProjectStatus.ACTIVE, response.getStatus());

        // Vérification de l'appel à la sauvegarde
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void testCreateProjectThrowsExceptionIfUserNotFound() {
        // [CONFIG SPÉCIFIQUE AU TEST] : L'utilisateur n'existe pas.
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // Vérification de l'exception
        assertThrows(UsernameNotFoundException.class, () -> projectService.createProject(TEST_EMAIL, creationRequest));

        // Vérification qu'aucune sauvegarde n'a eu lieu
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void testGetUserProjectsSuccess() {
        // [CONFIG SPÉCIFIQUE AU TEST] : L'utilisateur existe.
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(mockOwner));

        // Configuration des mocks pour la recherche de projets
        when(projectRepository.findByOwnerId(TEST_USER_ID)).thenReturn(List.of(mockProject));

        // Exécution
        var summaries = projectService.getUserProjects(TEST_EMAIL);

        // Vérifications
        assertFalse(summaries.isEmpty());
        assertEquals(1, summaries.size());
        assertEquals(mockProject.getName(), summaries.get(0).getName());
        verify(projectRepository, times(1)).findByOwnerId(TEST_USER_ID);
    }

    @Test
    void testGetProjectDetailsSuccess() {

        // Configuration des mocks
        when(projectRepository.findById(TEST_PROJECT_ID)).thenReturn(Optional.of(mockProject));

        // Exécution
        ProjectResponseDto response = projectService.getProjectDetails(TEST_PROJECT_ID, TEST_EMAIL);

        // Vérifications
        assertNotNull(response);
        assertEquals(TEST_PROJECT_ID, response.getId());
        assertEquals(mockProject.getName(), response.getName());
        verify(projectRepository, times(1)).findById(TEST_PROJECT_ID);
    }

    @Test
    void testGetProjectDetailsThrowsSecurityExceptionIfNotOwner() {

        // Projet mocké pour un autre propriétaire
        User otherOwner = User.builder().email("other@email.com").id(99L).build();
        Project otherProject = Project.builder()
                .id(11L)
                .owner(otherOwner)
                .build();

        when(projectRepository.findById(11L)).thenReturn(Optional.of(otherProject));

        // Exécution et vérification (l'utilisateur TEST_EMAIL essaie de voir le projet de 'other@email.com')
        assertThrows(SecurityException.class, () -> projectService.getProjectDetails(11L, TEST_EMAIL));
        verify(projectRepository, times(1)).findById(11L);
    }

    @Test
    void testUpdateProjectSuccess() {
        // GIVEN: L'utilisateur existe, le projet existe et lui appartient.
        when(projectRepository.findById(TEST_PROJECT_ID)).thenReturn(Optional.of(mockProject));
        when(projectRepository.save(any(Project.class))).thenReturn(mockProject);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(mockOwner));

        // Requête de mise à jour
        ProjectCreationRequestDto updateRequest = new ProjectCreationRequestDto();
        updateRequest.setName("Nom mis à jour");
        updateRequest.setDescription("Description mise à jour");

        // WHEN
        ProjectResponseDto response = projectService.updateProject(TEST_EMAIL, updateRequest, TEST_PROJECT_ID);

        // THEN
        assertNotNull(response);
        assertEquals("Nom mis à jour", response.getName());
        assertEquals("Description mise à jour", response.getDescription());

        // Vérification de la sauvegarde
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void testUpdateProjectThrowsAccessDeniedIfNotOwner() {
        // GIVEN: Le projet existe mais appartient à un autre utilisateur.

        User otherOwner = User.builder().email("other@email.com").id(99L).build();
        Project otherProject = Project.builder()
                .id(11L)
                .owner(otherOwner)
                .build();
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(mockOwner));
        when(projectRepository.findById(11L)).thenReturn(Optional.of(otherProject));

        // WHEN & THEN: L'utilisateur TEST_EMAIL ne peut pas le modifier.
        assertThrows(AccessDeniedException.class,
                () -> projectService.updateProject(TEST_EMAIL, creationRequest, 11L));

        // Vérification qu'aucune sauvegarde n'a eu lieu
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void testDeleteProjectSuccess() {
        // GIVEN: Le projet existe et appartient à l'utilisateur.
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(mockOwner));
        when(projectRepository.findById(TEST_PROJECT_ID)).thenReturn(Optional.of(mockProject));

        // WHEN
        projectService.deleteProject(TEST_EMAIL, TEST_PROJECT_ID);

        // THEN: Vérification de l'appel à la suppression
        verify(projectRepository, times(1)).delete(mockProject);
    }

    @Test
    void testDeleteProjectThrowsAccessDeniedIfNotOwner() {
        // GIVEN: Le projet existe mais appartient à un autre utilisateur.
        User otherOwner = User.builder().email("other@email.com").id(99L).build();
        Project otherProject = Project.builder()
                .id(11L)
                .owner(otherOwner)
                .build();
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(mockOwner));
        when(projectRepository.findById(11L)).thenReturn(Optional.of(otherProject));

        // WHEN & THEN: L'utilisateur TEST_EMAIL ne peut pas le supprimer.
        assertThrows(ResponseStatusException.class,
                () -> projectService.deleteProject(TEST_EMAIL, 11L));

        // Vérification qu'aucune suppression n'a eu lieu
        verify(projectRepository, never()).delete(any(Project.class));
    }
}

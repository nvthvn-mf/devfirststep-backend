package app.growject.service;

import app.growject.dto.TaskCreationRequestDto;
import app.growject.dto.TaskResponseDto;
import app.growject.entity.*;
import app.growject.repository.TaskRepository;
import app.growject.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Tests Unitaires pour TaskService.
 */
@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    private static final String TEST_EMAIL = "test.owner@growject.com";
    private static final String OTHER_EMAIL = "other.user@growject.com";
    private static final Long PROJECT_ID = 10L;
    private static final Long TASK_ID = 1L;

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProjectService projectService;

    @InjectMocks
    private TaskService taskService;

    private User mockOwner;
    private Project mockProject;
    private TaskCreationRequestDto creationRequest;
    private Task mockTask;

    @BeforeEach
    void setUp() {
        // 1. Owner Mock
        mockOwner = User.builder()
                .id(1L)
                .email(TEST_EMAIL)
                .name("Owner")
                .build();

        // 2. Project Mock (appartenant à mockOwner)
        mockProject = Project.builder()
                .id(PROJECT_ID)
                .owner(mockOwner)
                .name("Kanban Test Project")
                .build();

        // 3. Task Mock
        mockTask = Task.builder()
                .id(TASK_ID)
                .title("Création Entités")
                .status(TaskStatus.TO_DO)
                .orderIndex(0)
                .project(mockProject)
                .assignedTo(mockOwner)
                .createdAt(LocalDateTime.now())
                .build();

        // 4. Request DTO Mock
        creationRequest = TaskCreationRequestDto.builder()
                .title("Nouvelle Tâche")
                .description("Description de la nouvelle tâche")
                .status(TaskStatus.IN_PROGRESS)
                .orderIndex(1)
                .assignedUserId(mockOwner.getId())
                .build();
    }

    /**
     * Helper pour simuler la vérification de propriété par le TaskService
     */
    private void mockOwnerCheckSuccess() {
        // 1. Simuler que l'utilisateur existe
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(mockOwner));
        // 2. Simuler que le ProjectService retourne le projet (vérification réussie)
        when(projectService.getProjectEntityById(PROJECT_ID)).thenReturn(Optional.of(mockProject));
    }

    // --- CREATE TESTS ---

    @Test
    void testCreateTaskSuccess() {
        // GIVEN
        mockOwnerCheckSuccess();
        // Simuler la recherche de l'utilisateur assigné (c'est l'Owner lui-même ici)
        when(userRepository.findById(mockOwner.getId())).thenReturn(Optional.of(mockOwner));
        // Simuler la sauvegarde
        when(taskRepository.save(any(Task.class))).thenReturn(mockTask);

        // WHEN
        TaskResponseDto response = taskService.createTask(PROJECT_ID, TEST_EMAIL, creationRequest);

        // THEN
        assertNotNull(response);
        assertEquals(TASK_ID, response.getId());
        assertEquals(PROJECT_ID, response.getProjectId());
        assertEquals(TaskStatus.TO_DO, response.getStatus());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void testCreateTaskThrowsAccessDeniedIfNotOwner() {
        // GIVEN: Simuler que l'utilisateur est trouvé mais n'est PAS le propriétaire du projet
        when(userRepository.findByEmail(OTHER_EMAIL)).thenReturn(Optional.of(mock(User.class)));
        when(projectService.getProjectEntityById(PROJECT_ID)).thenReturn(Optional.of(mockProject));

        // WHEN & THEN
        assertThrows(AccessDeniedException.class,
                () -> taskService.createTask(PROJECT_ID, OTHER_EMAIL, creationRequest));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void testCreateTaskThrowsNotFoundIfProjectDoesNotExist() {
        // GIVEN: Simuler que le projet n'est pas trouvé
        //when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(mockOwner));
        //when(projectService.getProjectEntityById(PROJECT_ID)).thenReturn(Optional.empty());

        // WHEN & THEN
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> taskService.createTask(PROJECT_ID, TEST_EMAIL, creationRequest));
        assertEquals(NOT_FOUND, exception.getStatusCode());
        verify(taskRepository, never()).save(any(Task.class));
    }

    // --- GET TESTS ---

    @Test
    void testGetTasksByProjectSuccess() {
        // GIVEN
        mockOwnerCheckSuccess();
        List<Task> tasks = List.of(mockTask);
        when(taskRepository.findByProjectId(PROJECT_ID)).thenReturn(tasks);

        // WHEN
        List<TaskResponseDto> response = taskService.getTasksByProject(PROJECT_ID, TEST_EMAIL);

        // THEN
        assertFalse(response.isEmpty());
        assertEquals(1, response.size());
        assertEquals(TASK_ID, response.get(0).getId());
    }

    @Test
    void testGetTasksThrowsAccessDeniedIfNotOwner() {
        // GIVEN: Simuler un utilisateur non propriétaire
        when(userRepository.findByEmail(OTHER_EMAIL)).thenReturn(Optional.of(mock(User.class)));
        when(projectService.getProjectEntityById(PROJECT_ID)).thenReturn(Optional.of(mockProject));

        // WHEN & THEN
        assertThrows(AccessDeniedException.class,
                () -> taskService.getTasksByProject(PROJECT_ID, OTHER_EMAIL));
        verify(taskRepository, never()).findByProjectId(any());
    }

    // --- UPDATE TESTS ---

    @Test
    void testUpdateTaskStatusSuccess() {
        // GIVEN
        mockOwnerCheckSuccess();
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(mockTask));

        // Simuler la mise à jour (Drag & Drop)
        TaskCreationRequestDto updateRequest = TaskCreationRequestDto.builder()
                .status(TaskStatus.DONE)
                .orderIndex(5)
                .build();

        // Simuler la tâche mise à jour
        Task updatedTask = mockTask;
        updatedTask.setStatus(TaskStatus.DONE);
        updatedTask.setOrderIndex(5);
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

        // WHEN
        TaskResponseDto response = taskService.updateTask(PROJECT_ID, TASK_ID, TEST_EMAIL, updateRequest);

        // THEN
        assertNotNull(response);
        assertEquals(TaskStatus.DONE, response.getStatus());
        assertEquals(5, response.getOrderIndex());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void testUpdateTaskThrowsNotFoundIfTaskNotBelongToProject() {
        // GIVEN: Projet bien possédé, mais la tâche n'est pas liée au bon projet ID
        mockOwnerCheckSuccess();

        Task taskFromOtherProject = Task.builder()
                .id(2L)
                .project(Project.builder().id(99L).build()) // Projet 99
                .build();

        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(taskFromOtherProject));

        // WHEN & THEN: Tentative de modifier la tâche 2L dans le projet 10L
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> taskService.updateTask(PROJECT_ID, TASK_ID, TEST_EMAIL, creationRequest));
        assertEquals(NOT_FOUND, exception.getStatusCode());
        verify(taskRepository, never()).save(any(Task.class));
    }

    // --- DELETE TESTS ---

    @Test
    void testDeleteTaskSuccess() {
        // GIVEN
        mockOwnerCheckSuccess();
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(mockTask));
        // Pas besoin de mock pour la suppression, juste vérifier l'appel
        doNothing().when(taskRepository).delete(mockTask);

        // WHEN
        taskService.deleteTask(PROJECT_ID, TASK_ID, TEST_EMAIL);

        // THEN
        verify(taskRepository, times(1)).delete(mockTask);
    }

    @Test
    void testDeleteTaskThrowsAccessDeniedIfNotOwner() {
        // GIVEN: Simuler un utilisateur non propriétaire
        when(userRepository.findByEmail(OTHER_EMAIL)).thenReturn(Optional.of(mock(User.class)));
        when(projectService.getProjectEntityById(PROJECT_ID)).thenReturn(Optional.of(mockProject));

        // WHEN & THEN
        assertThrows(AccessDeniedException.class,
                () -> taskService.deleteTask(PROJECT_ID, TASK_ID, OTHER_EMAIL));
        verify(taskRepository, never()).delete(any());
    }
}

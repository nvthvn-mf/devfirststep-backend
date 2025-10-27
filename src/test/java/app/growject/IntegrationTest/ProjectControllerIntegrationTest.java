package app.growject.IntegrationTest;

import app.growject.dto.AuthenticationResponse;
import app.growject.dto.LoginRequestDto;
import app.growject.dto.ProjectCreationRequestDto;
import app.growject.dto.ProjectResponseDto;
import app.growject.dto.RegisterRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test d'intégration simulant les opérations CRUD de projet.
 * Le profil 'test' est souvent utilisé pour garantir l'usage d'une DB H2 propre.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Assurez-vous d'avoir un application-test.properties pour H2
public class ProjectControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String AUTH_URL = "/api/v1/auth";
    private static final String PROJECTS_URL = "/api/v1/projects";
    private static final String TEST_EMAIL = "project.test@growject.com";
    private static final String TEST_PASSWORD = "Password123!";
    private String jwtToken; // Le token JWT récupéré après connexion

    @BeforeEach
    void setUp() throws Exception {
        // Initialiser l'utilisateur et récupérer le token une seule fois
        jwtToken = registerAndLogin(TEST_EMAIL, TEST_PASSWORD);
    }

    /**
     * Helper pour s'inscrire et se connecter, retournant le token JWT.
     */
    private String registerAndLogin(String email, String password) throws Exception {
        // 1. Inscription (on peut ignorer l'erreur si l'utilisateur existe déjà)
        RegisterRequestDto registerRequest = RegisterRequestDto.builder()
                .name("Project Tester")
                .email(email)
                .password(password)
                .build();

        mockMvc.perform(post(AUTH_URL + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // 2. Connexion
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        MvcResult loginResult = mockMvc.perform(post(AUTH_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(loginResult.getResponse().getContentAsString(), AuthenticationResponse.class).getToken();
    }

    @Test
    void testCreateAndGetProjectFlow() throws Exception {
        // 1. Création d'un projet
        ProjectCreationRequestDto createRequest = new ProjectCreationRequestDto();
        createRequest.setName("First Project Test");
        createRequest.setDescription("Testing project creation.");
        createRequest.setStacks(List.of("Java", "PostgreSQL"));

        MvcResult createResult = mockMvc.perform(post(PROJECTS_URL)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("First Project Test"))
                .andExpect(jsonPath("$.owner.email").value(TEST_EMAIL))
                .andReturn();

        ProjectResponseDto createdProject = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), ProjectResponseDto.class);
        assertNotNull(createdProject.getId());
        Long projectId = createdProject.getId();

        // 2. Récupération de la liste des projets
        mockMvc.perform(get(PROJECTS_URL)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("First Project Test"))
                .andReturn();

        // 3. Récupération des détails du projet
        mockMvc.perform(get(PROJECTS_URL + "/" + projectId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Testing project creation."))
                .andReturn();

        // 4. Test d'accès refusé (sans token)
        mockMvc.perform(get(PROJECTS_URL + "/" + projectId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}

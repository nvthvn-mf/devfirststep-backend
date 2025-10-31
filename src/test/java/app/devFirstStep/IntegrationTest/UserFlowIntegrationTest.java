package app.devFirstStep.IntegrationTest;

import app.devFirstStep.dto.AuthenticationResponse;
import app.devFirstStep.dto.LoginRequestDto;
import app.devFirstStep.dto.ProfileUpdateRequestDto;
import app.devFirstStep.dto.RegisterRequestDto;
import app.devFirstStep.dto.UserResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test d'intégration simulant le flux complet utilisateur :
 * 1. Inscription
 * 2. Connexion (obtention du token)
 * 3. Récupération du profil (route protégée)
 * 4. Mise à jour du profil (route protégée)
 */
@SpringBootTest
@AutoConfigureMockMvc // Pour injecter MockMvc
public class UserFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc; // Utilisé pour simuler les requêtes HTTP

    @Autowired
    private ObjectMapper objectMapper; // Utilisé pour convertir les objets Java en JSON

    private static final String AUTH_URL = "/api/v1/auth";
    private static final String USER_URL = "/api/v1/user/profile";
    private static final String TEST_EMAIL = "integration.test@growject.com";
    private static final String TEST_PASSWORD = "Password123!";

    /**
     * Exécute le flux complet d'authentification et de gestion de profil.
     */
    @Test
    void testFullUserProfileFlow() throws Exception {
        // --- 1. INSCRIPTION ---
        RegisterRequestDto registerRequest = RegisterRequestDto.builder()
                .name("Integration Tester")
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .bio("Initial bio.")
                .skills(List.of("Java", "Spring Boot"))
                .build();

        MvcResult registerResult = mockMvc.perform(post(AUTH_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        // --- 2. CONNEXION (et récupération du TOKEN) ---
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword(TEST_PASSWORD);

        MvcResult loginResult = mockMvc.perform(post(AUTH_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        // Récupérer le token pour les requêtes protégées
        String token = objectMapper.readValue(loginResult.getResponse().getContentAsString(), AuthenticationResponse.class).getToken();
        assertNotNull(token);


        // --- 3. RÉCUPÉRATION DU PROFIL (GET Protégé) ---
        MvcResult getProfileResult = mockMvc.perform(get(USER_URL)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.level").value("BEGINNER")) // Vérifie le niveau par défaut
                .andReturn();

        UserResponseDto initialProfile = objectMapper.readValue(getProfileResult.getResponse().getContentAsString(), UserResponseDto.class);
        assertNotNull(initialProfile.getId());


        // --- 4. MISE À JOUR DU PROFIL (PUT Protégé) ---
        ProfileUpdateRequestDto updateRequest = ProfileUpdateRequestDto.builder()
                .bio("Bio mise à jour pour le test d'intégration.")
                .skills(List.of("Java", "Spring Boot", "Testing")) // Ajout de "Testing"
                .build();

        MvcResult updateResult = mockMvc.perform(put(USER_URL)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.bio").value("Bio mise à jour pour le test d'intégration."))
                .andReturn();

        UserResponseDto updatedProfile = objectMapper.readValue(updateResult.getResponse().getContentAsString(), UserResponseDto.class);
        assertTrue(updatedProfile.getSkills().contains("Testing"));

        // --- 5. TENTATIVE D'ACCÈS SANS TOKEN (Doit échouer) ---
        mockMvc.perform(get(USER_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}


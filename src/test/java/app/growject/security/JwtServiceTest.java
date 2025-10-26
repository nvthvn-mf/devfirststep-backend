package app.growject.security;

import app.growject.entity.DeveloperLevel;
import app.growject.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests Unitaires pour JwtService.
 * JwtServiceTest : Teste la génération et la validité du token.
 * Utilise ReflectionTestUtils pour injecter manuellement les valeurs @Value.
 */
@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private User userDetails;
    private final String TEST_SECRET = "9f8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a3b"; // Clé de test
    private final long EXPIRATION_TIME = 3600000; // 1 heure

    @BeforeEach
    void setUp() {
        // 1. Initialiser le service avec les valeurs de test
        ReflectionTestUtils.setField(jwtService, "SECRET_KEY", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "EXPIRATION_TIME", EXPIRATION_TIME);

        // 2. Créer un utilisateur de mock pour les tests
        userDetails = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@growject.com")
                .password("encoded_password")
                .skills(Collections.emptyList())
                .level(DeveloperLevel.BEGINNER)
                .build();
    }

    @Test
    void testGenerateAndExtractUsername() {
        // 1. Générer le token
        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);

        // 2. Extraire l'email et vérifier la correspondance
        String extractedEmail = jwtService.extractUsername(token);
        assertEquals(userDetails.getUsername(), extractedEmail);
    }

    @Test
    void testIsTokenValid() {
        // 1. Générer le token
        String token = jwtService.generateToken(userDetails);

        // 2. Vérifier la validité pour le bon utilisateur
        assertTrue(jwtService.isTokenValid(token, userDetails));

        // 3. Créer un utilisateur différent
        UserDetails differentUser = User.builder().email("other@growject.com").password("pwd").build();

        // 4. Vérifier l'invalidité pour un utilisateur différent
        assertFalse(jwtService.isTokenValid(token, differentUser));
    }
}

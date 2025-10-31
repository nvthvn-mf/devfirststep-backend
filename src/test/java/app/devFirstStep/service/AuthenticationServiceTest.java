package app.devFirstStep.service;

import app.devFirstStep.dto.AuthenticationResponse;
import app.devFirstStep.dto.LoginRequestDto;
import app.devFirstStep.dto.RegisterRequestDto;
import app.devFirstStep.entity.User;
import app.devFirstStep.repository.UserRepository;
import app.devFirstStep.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests Unitaires pour AuthenticationService.
 * Ce test isole la logique de l'AuthenticationService,
 * en simulant les dépendances (UserRepository, AuthenticationManager, etc.).
 */
@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    private RegisterRequestDto registerRequest;
    private LoginRequestDto loginRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequestDto();
        registerRequest.setEmail("newuser@test.com");
        registerRequest.setPassword("securepwd");
        registerRequest.setName("New User");

        loginRequest = new LoginRequestDto();
        loginRequest.setEmail("existing@test.com");
        loginRequest.setPassword("securepwd");

        mockUser = User.builder().email("existing@test.com").password("encoded_pwd").build();
    }

    @Test
    void testRegisterSuccess() {
        // Préparation des mocks
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(any(String.class))).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]); // Retourne l'objet sauvegardé
        when(jwtService.generateToken(any(User.class))).thenReturn("mocked_jwt_token");

        // Exécution
        AuthenticationResponse response = authenticationService.register(registerRequest);

        // Vérification
        assertNotNull(response);
        assertEquals("mocked_jwt_token", response.getToken());

        // Vérification que le mot de passe a été encodé avant la sauvegarde
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        assertEquals("hashed_password", userCaptor.getValue().getPassword());
    }

    @Test
    void testRegisterThrowsExceptionIfEmailExists() {
        // Préparation des mocks
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // Exécution et Vérification (attend une exception)
        assertThrows(IllegalArgumentException.class, () -> authenticationService.register(registerRequest));

        // Vérification qu'aucune sauvegarde n'a eu lieu
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testAuthenticateSuccess() {
        // Préparation des mocks
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(mockUser));
        when(jwtService.generateToken(mockUser)).thenReturn("mocked_jwt_token_login");

        // Exécution
        AuthenticationResponse response = authenticationService.authenticate(loginRequest);

        // Vérification
        assertNotNull(response);
        assertEquals("mocked_jwt_token_login", response.getToken());

        // Vérification que l'AuthenticationManager a été appelé (validation du mot de passe)
        verify(authenticationManager, times(1)).authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
    }
}

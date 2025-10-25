package app.growject.service;

import app.growject.dto.AuthenticationResponse;
import app.growject.dto.LoginRequestDto;
import app.growject.dto.RegisterRequestDto;
import app.growject.entity.DeveloperLevel;
import app.growject.entity.User;
import app.growject.repository.UserRepository;
import app.growject.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // 1. Logique d'Inscription
    public AuthenticationResponse register(RegisterRequestDto request) {
        // TODO: Vérifier si l'utilisateur existe déjà par email
        if (repository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("L'email est déjà utilisé.");
        }

        // Créer l'entité User à partir du DTO
        var user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                // TODO: Crypter le mot de passe avant de le sauvegarder
                .password(passwordEncoder.encode(request.getPassword()))
                .bio(request.getBio())
                .skills(request.getSkills() != null ? request.getSkills() : List.of())
                .level(DeveloperLevel.BEGINNER) // Niveau par défaut
                .build();

        // Sauvegarder dans la base de données
        repository.save(user);

        // Générer le token JWT pour l'utilisateur nouvellement inscrit
        var jwtToken = jwtService.generateToken(user);

        // Retourner le token
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    // 2. Logique de Connexion
    public AuthenticationResponse authenticate(LoginRequestDto request) {
        // 1. Tenter d'authentifier l'utilisateur via l'AuthenticationManager
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), // Utilisé comme username
                        request.getPassword()
                )
        );
        // Si la ligne ci-dessus n'a PAS lancé d'exception, l'utilisateur est authentifié.

        // 2. Récupérer l'objet User (pour les détails et le JWT)
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email ou mot de passe incorrect."));

        // 3. Générer le token JWT
        var jwtToken = jwtService.generateToken(user);

        // 4. Retourner le token
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
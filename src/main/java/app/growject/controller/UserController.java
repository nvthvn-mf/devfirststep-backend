package app.growject.controller;

import app.growject.dto.ProfileUpdateRequestDto;
import app.growject.dto.UserResponseDto; // Nous allons créer ce DTO pour la réponse
import app.growject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Récupère le profil de l'utilisateur actuellement authentifié.
     * Le token JWT dans l'en-tête est utilisé pour identifier l'utilisateur via @AuthenticationPrincipal.
     */
    @GetMapping("/profile")
    public ResponseEntity<UserResponseDto> getProfile(
            // Le UserDetails (votre entité User) est injecté directement par Spring Security.
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // Le nom d'utilisateur (email) est récupéré, puis le service cherche l'utilisateur complet
        UserResponseDto profile = userService.getProfile(userDetails.getUsername());
        return ResponseEntity.ok(profile);
    }

    /**
     * Met à jour le profil de l'utilisateur actuellement authentifié.
     */
    @PutMapping("/profile")
    public ResponseEntity<UserResponseDto> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ProfileUpdateRequestDto request
    ) {
        // Le service gère la logique de mise à jour et retourne le DTO mis à jour
        UserResponseDto updatedProfile = userService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(updatedProfile);
    }

    // TODO: Vous pouvez ajouter ici d'autres méthodes (ex: changer le mot de passe)
}

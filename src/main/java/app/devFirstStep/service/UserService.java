package app.devFirstStep.service;

import app.devFirstStep.dto.ProfileUpdateRequestDto;
import app.devFirstStep.dto.UserResponseDto;
import app.devFirstStep.entity.User;
import app.devFirstStep.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Récupère le profil d'un utilisateur par son email (username).
     */
    public UserResponseDto getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé."));

        return mapToUserResponseDto(user);
    }


    /**
     * Met à jour les informations de profil de l'utilisateur.
     */

    public UserResponseDto updateProfile(String email, ProfileUpdateRequestDto request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé."));

        // 1. Mettre à jour les champs
        if (request.getName() != null) user.setName(request.getName());
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());

        // Mise à jour des compétences
        if (request.getSkills() != null) user.setSkills(request.getSkills());

        // Mise à jour du niveau (si l'utilisateur a cette permission, ou si c'est auto-attribué)
        if (request.getLevel() != null) user.setLevel(request.getLevel());

        // 2. Sauvegarder
        User updatedUser = userRepository.save(user);

        // 3. Mapper et retourner
        return mapToUserResponseDto(updatedUser);
    }

    /**
     * Mapper l'entité User vers le DTO de réponse UserResponseDto
     */
    private UserResponseDto mapToUserResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .tag(user.getTag())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .skills(user.getSkills())
                .level(user.getLevel().name()) // <-- Conversion Enum to String
                .build();
    }
}

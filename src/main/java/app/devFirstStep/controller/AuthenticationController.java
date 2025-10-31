package app.devFirstStep.controller;

import app.devFirstStep.dto.AuthenticationResponse;
import app.devFirstStep.dto.LoginRequestDto;
import app.devFirstStep.dto.RegisterRequestDto;
import app.devFirstStep.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Définit le chemin de base et indique qu'il s'agit d'un contrôleur REST
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequestDto request
    ) {
        // TODO: Appeler la méthode register du service et retourner la réponse 200 OK
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody LoginRequestDto request
    ) {
        // TODO: Appeler la méthode authenticate du service et retourner la réponse 200 OK
        return ResponseEntity.ok(service.authenticate(request));
    }
}
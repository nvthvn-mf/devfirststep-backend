package app.devFirstStep.config;

import app.devFirstStep.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    // TODO: Vous aurez besoin d'injecter ici le JwtAuthenticationFilter (à créer plus tard)
    private final JwtAuthenticationFilter jwtAuthFilter;

    // TODO: Et l'AuthenticationProvider (qui sera configuré pour utiliser votre UserDetailsService)
    private final AuthenticationProvider authenticationProvider;


    /**
     * Définit la chaîne de filtres de sécurité.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Désactive le CSRF (nécessaire pour les API REST stateless)
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        // Permet l'accès public à l'API d'authentification (register, login)
                        .requestMatchers(
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/api/v1/auth/ping"

                        ).permitAll()

                        // Toutes les autres requêtes nécessitent une authentification
                        .anyRequest().authenticated()
                )

                // Configure la gestion de session comme STATELESS (pas de sessions côté serveur, JWT gère l'état)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configure le fournisseur d'authentification
                .authenticationProvider(authenticationProvider)

                // Ajoute votre filtre JWT AVANT le filtre de vérification standard
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
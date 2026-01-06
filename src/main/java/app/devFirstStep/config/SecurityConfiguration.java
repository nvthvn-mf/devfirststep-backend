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

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;

    private final AuthenticationProvider authenticationProvider;

    /**
     * Configuration du bean CORS.
     * Autorise les requêtes depuis l'origine de développement Vue.js (5173).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Autoriser l'origine du serveur de développement frontend
        configuration.setAllowedOrigins(List.of("http://localhost:5174", "http://localhost:5173"));

        // Autoriser les méthodes HTTP standard (POST pour inscription/connexion)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Autoriser tous les headers (y compris Authorization pour le JWT)
        configuration.setAllowedHeaders(List.of("*"));

        // Permettre l'envoi de cookies/credentials (si JWT est stocké dans un cookie, ce qui n'est pas notre cas, mais bonne pratique)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Appliquer cette configuration à toutes les routes (/**)
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    /**
     * Définit la chaîne de filtres de sécurité.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Appliquer la configuration CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. Désactiver le CSRF
                .csrf(AbstractHttpConfigurer::disable)

                // 3. Définir les règles d'autorisation
                .authorizeHttpRequests(auth -> auth
                        // Permet l'accès public à l'API d'authentification et aux requêtes OPTIONS (Preflight CORS)
                        .requestMatchers("/api/v1/auth/**", "/api/v1/auth/**").permitAll()

                        // Toutes les autres requêtes nécessitent une authentification
                        .anyRequest().authenticated()
                )

                // 4. Configurer la gestion de session comme STATELESS (JWT)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 5. Configurer le fournisseur d'authentification
                .authenticationProvider(authenticationProvider)

                // 6. Ajouter le filtre JWT AVANT le filtre de vérification standard
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
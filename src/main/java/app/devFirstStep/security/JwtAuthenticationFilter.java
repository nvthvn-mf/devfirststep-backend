package app.devFirstStep.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    // Le UserDetailsService est le bean que nous avons défini dans ApplicationConfig
    // qui utilise votre UserRepository pour charger l'utilisateur par email.

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Récupérer l'en-tête Authorization
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail; // C'est l'email que nous utilisons comme username

        // 2. Vérifier si l'en-tête est absent ou ne commence pas par "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraire le token (supprimer "Bearer ")
        jwt = authHeader.substring(7);

        // 4. Extraire l'email (nom d'utilisateur)
        userEmail = jwtService.extractUsername(jwt);

        // 5. Vérifier si l'email est là ET si l'utilisateur n'est PAS déjà authentifié
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Charger les détails de l'utilisateur depuis la DB
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 6. Valider le token
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // 7. Créer un objet d'authentification pour mettre à jour le contexte de sécurité
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // Nous n'avons pas besoin du mot de passe ici (déjà vérifié)
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 8. Mettre à jour le contexte de sécurité (l'utilisateur est maintenant authentifié)
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Passer au filtre suivant (ou à la servlet/contrôleur si c'est le dernier)
        filterChain.doFilter(request, response);
    }
}
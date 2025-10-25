package app.growject.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // Injecté depuis application.properties
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    // Injecté depuis application.properties (en ms)
    @Value("${jwt.expiration}")
    private long EXPIRATION_TIME;

    // ------------------------------------------
    // 1. EXTRACTION & VALIDATION
    // ------------------------------------------

    /** Extrait le "username" (l'email dans notre cas) du token. */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** Vérifie si le token est valide pour l'utilisateur donné. */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // Le nom d'utilisateur correspond ET le token n'est pas expiré
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /** Vérifie si le token a expiré. */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /** Extrait la date d'expiration du token. */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /** Fonction utilitaire pour extraire un claim spécifique (comme l'email ou l'expiration). */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /** Analyse le token pour récupérer toutes les revendications (claims). */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey()) // Utilise la clé secrète pour valider la signature
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ------------------------------------------
    // 2. GÉNÉRATION
    // ------------------------------------------

    /** Génère un token simple à partir des détails de l'utilisateur. */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /** Génère un token avec des claims supplémentaires (comme le rôle). */
    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims) // Ajoute les claims supplémentaires
                .setSubject(userDetails.getUsername()) // Le sujet (email)
                .setIssuedAt(new Date(System.currentTimeMillis())) // Date de création
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // Date d'expiration
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // Signature avec la clé secrète et l'algorithme HS256
                .compact();
    }

    // ------------------------------------------
    // 3. CLÉ SECRÈTE
    // ------------------------------------------

    /** Récupère la clé de signature encodée en BASE64 à partir de SECRET_KEY. */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
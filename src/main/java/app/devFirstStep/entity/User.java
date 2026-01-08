package app.devFirstStep.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Getter // Remplace @Data pour un contrôle plus fin
@Setter
@Builder // Nécessite @AllArgsConstructor pour fonctionner correctement
@NoArgsConstructor // Requis par JPA pour le constructeur sans arguments
@AllArgsConstructor // Requis par @Builder pour générer le constructeur complet
@Table(name = "users")

public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String tag;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // On stockera le mot de passe crypté

    @Column(length = 500) // Limite la longueur de la bio
    private String bio;

    private String avatarUrl;

    // Pour une liste simple de chaînes de caractères, @ElementCollection est parfait pour un MVP.
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> skills;

    // Pour le niveau, un Enum est plus propre qu'un String.
    @Enumerated(EnumType.STRING)
    private DeveloperLevel level;
    // Un constructeur vide est requis par JPA.


    /* ****************************************************** */
    /* Implémentation des méthodes de l'interface UserDetails */
    /* ****************************************************** */

    // Pour l'instant, on attribue un rôle de base (USER) à tous.
    // On ajoutera une colonne 'role' dans User pour une gestion plus fine (Owner/Collaborator plus tard).
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    // L'email est utilisé comme nom d'utilisateur pour la connexion
    @Override
    public String getUsername() {
        return email;
    }

    // Gère si le compte est actif ou désactivé. Par défaut : true.
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // Gère le verrouillage du compte. Par défaut : true.
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // Gère l'expiration des identifiants (mot de passe). Par défaut : true.
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // Gère si l'utilisateur est activé (ex: après validation e-mail). Par défaut : true.
    @Override
    public boolean isEnabled() {
        return true;
    }
}


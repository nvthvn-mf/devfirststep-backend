package app.growject.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

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

    // Getters et Setters...
    // Un constructeur vide est requis par JPA.
    public User() {}

}


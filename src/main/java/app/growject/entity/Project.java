package app.growject.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class) // Permet d'utiliser @CreatedDate/etc
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Lob // Pour stocker de longues descriptions
    private String description;

    @Lob
    private String objectives;

    // Stacks utilisées (élément central de GrowJect)
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> stacks;

    // Lien vers le propriétaire du projet
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status = ProjectStatus.DRAFT; // Statut par défaut

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Une collection de tâches est utile pour la navigation bidirectionnelle, mais
    // nous préférerons interroger la DB via TaskRepository pour le Kanban.
    // @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<Task> tasks;
}

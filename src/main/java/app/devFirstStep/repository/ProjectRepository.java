package app.devFirstStep.repository;

import app.devFirstStep.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Trouver tous les projets appartenant à un utilisateur spécifique (par ID du propriétaire)
    List<Project> findByOwnerId(Long ownerId);

    // Dans MVP+, on pourrait avoir findByCollaboratorId, mais pas nécessaire pour l'instant.
}

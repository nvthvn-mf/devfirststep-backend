package app.growject.repository;

import app.growject.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Trouver toutes les tâches pour un projet donné, triées par orderIndex (pour le Kanban)
    List<Task> findByProjectIdOrderByOrderIndexAsc(Long projectId);

    // Trouver la tâche de plus haut index pour un projet et un statut donnés, pour insérer la prochaine
    // Nécessaire pour optimiser l'insertion de nouvelles tâches
    Task findTopByProjectIdAndStatusOrderByOrderIndexDesc(Long projectId, app.growject.entity.TaskStatus status);

    List<Task> findByProjectId(Long projectId);

}

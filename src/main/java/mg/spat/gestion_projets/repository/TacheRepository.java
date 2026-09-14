package mg.spat.gestion_projets.repository;

import mg.spat.gestion_projets.entity.StatutTache;
import mg.spat.gestion_projets.entity.Tache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TacheRepository extends JpaRepository<Tache, Long> {

    List<Tache> findByProjetId(Long projetId);

    List<Tache> findByProjetIdAndStatut(Long projetId, StatutTache statut);

    List<Tache> findByAssigneAId(Long utilisateurId);

    List<Tache> findByTacheParentId(Long tacheParentId);

    long countByProjetIdAndStatut(Long projetId, StatutTache statut);

    @Query("SELECT t FROM Tache t WHERE t.projet.id = :projetId "
         + "AND t.dateEcheance < CURRENT_DATE AND t.statut <> 'TERMINEE'")
    List<Tache> findTachesEnRetard(@Param("projetId") Long projetId);

    @Query("SELECT t.statut, COUNT(t) FROM Tache t WHERE t.projet.id = :projetId GROUP BY t.statut")
    List<Object[]> compterParStatut(@Param("projetId") Long projetId);
}

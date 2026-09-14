package mg.spat.gestion_projets.repository;

import mg.spat.gestion_projets.entity.Projet;
import mg.spat.gestion_projets.entity.StatutProjet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjetRepository extends JpaRepository<Projet, Long> {

    List<Projet> findByResponsableId(Long responsableId);

    List<Projet> findByStatut(StatutProjet statut);

    @Query("SELECT p FROM Projet p JOIN p.membres m WHERE m.id = :utilisateurId")
    List<Projet> findProjetsDeLUtilisateur(@Param("utilisateurId") Long utilisateurId);

    @Query("SELECT p FROM Projet p WHERE p.dateFin < CURRENT_DATE AND p.statut <> 'TERMINE'")
    List<Projet> findProjetsEnRetard();
}

package mg.spat.gestion_projets.repository;

import mg.spat.gestion_projets.entity.SuiviTemps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SuiviTempsRepository extends JpaRepository<SuiviTemps, Long> {

    List<SuiviTemps> findByTacheIdOrderByDateTravailDesc(Long tacheId);

    List<SuiviTemps> findByUtilisateurIdOrderByDateTravailDesc(Long utilisateurId);

    /** Total des minutes saisies sur une tache. */
    @Query("SELECT COALESCE(SUM(s.dureeMinutes), 0) FROM SuiviTemps s "
         + "WHERE s.tache.id = :tacheId")
    int totalMinutesParTache(@Param("tacheId") Long tacheId);

    /** Total des minutes saisies sur tout un projet. */
    @Query("SELECT COALESCE(SUM(s.dureeMinutes), 0) FROM SuiviTemps s "
         + "WHERE s.tache.projet.id = :projetId")
    int totalMinutesParProjet(@Param("projetId") Long projetId);

    /** Total des minutes d'une personne sur un projet. */
    @Query("SELECT COALESCE(SUM(s.dureeMinutes), 0) FROM SuiviTemps s "
         + "WHERE s.tache.projet.id = :projetId AND s.utilisateur.id = :utilisateurId")
    int totalMinutesParMembreEtProjet(@Param("projetId") Long projetId,
                                      @Param("utilisateurId") Long utilisateurId);

    /** Ce qu'une personne a deja saisi un jour donne, toutes taches confondues. */
    @Query("SELECT COALESCE(SUM(s.dureeMinutes), 0) FROM SuiviTemps s "
         + "WHERE s.utilisateur.id = :utilisateurId AND s.dateTravail = :jour")
    int totalMinutesDuJour(@Param("utilisateurId") Long utilisateurId,
                           @Param("jour") LocalDate jour);

    /** Saisies d'un projet entier, pour le recapitulatif. */
    @Query("SELECT s FROM SuiviTemps s WHERE s.tache.projet.id = :projetId "
         + "ORDER BY s.dateTravail DESC")
    List<SuiviTemps> findByProjetId(@Param("projetId") Long projetId);
}

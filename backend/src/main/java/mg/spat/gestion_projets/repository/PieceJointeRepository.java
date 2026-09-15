package mg.spat.gestion_projets.repository;

import mg.spat.gestion_projets.entity.PieceJointe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PieceJointeRepository extends JpaRepository<PieceJointe, Long> {

    List<PieceJointe> findByTacheIdOrderByDateDepotDesc(Long tacheId);

    long countByTacheId(Long tacheId);

    @Query("SELECT COALESCE(SUM(p.tailleOctets), 0) FROM PieceJointe p "
         + "WHERE p.tache.projet.id = :projetId")
    long totalOctetsParProjet(@Param("projetId") Long projetId);
}

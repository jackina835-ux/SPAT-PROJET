package mg.spat.gestion_projets.repository;

import mg.spat.gestion_projets.entity.Commentaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentaireRepository extends JpaRepository<Commentaire, Long> {

    List<Commentaire> findByTacheIdOrderByDateCreationAsc(Long tacheId);

    List<Commentaire> findByAuteurId(Long auteurId);

    long countByTacheId(Long tacheId);
}

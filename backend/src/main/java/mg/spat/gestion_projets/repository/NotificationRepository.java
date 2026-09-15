package mg.spat.gestion_projets.repository;

import mg.spat.gestion_projets.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByDestinataireIdOrderByDateCreationDesc(
            Long destinataireId, Pageable pageable);

    List<Notification> findByDestinataireIdAndLuFalseOrderByDateCreationDesc(
            Long destinataireId);

    long countByDestinataireIdAndLuFalse(Long destinataireId);

    @Modifying
    @Query("UPDATE Notification n SET n.lu = true "
         + "WHERE n.destinataire.id = :destinataireId AND n.lu = false")
    int marquerToutLu(@Param("destinataireId") Long destinataireId);
}

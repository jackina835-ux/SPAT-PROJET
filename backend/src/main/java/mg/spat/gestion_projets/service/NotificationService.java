package mg.spat.gestion_projets.service;

import mg.spat.gestion_projets.dto.NotificationDTO;
import mg.spat.gestion_projets.entity.Notification;
import mg.spat.gestion_projets.entity.TypeNotification;
import mg.spat.gestion_projets.entity.Utilisateur;
import mg.spat.gestion_projets.exception.RegleMetierException;
import mg.spat.gestion_projets.exception.RessourceIntrouvableException;
import mg.spat.gestion_projets.repository.NotificationRepository;
import mg.spat.gestion_projets.security.ServiceSecurite;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Creation et lecture des notifications.
 *
 * Regle constante : on ne se notifie jamais soi-meme.
 * Quand Rado assigne une tache a Miora, seule Miora est
 * prevenue. Rado sait deja ce qu'il vient de faire.
 */
@Service
@Transactional
public class NotificationService {

    /** Nombre de notifications renvoyees dans l'historique. */
    private static final int TAILLE_HISTORIQUE = 30;

    private final NotificationRepository notificationRepository;
    private final ServiceSecurite serviceSecurite;

    public NotificationService(NotificationRepository notificationRepository,
                               ServiceSecurite serviceSecurite) {
        this.notificationRepository = notificationRepository;
        this.serviceSecurite = serviceSecurite;
    }

    // ---------- Creation ----------

    /**
     * Enregistre une notification, sauf si le destinataire est
     * la personne qui declenche l'action.
     */
    public void notifier(Utilisateur destinataire,
                         TypeNotification type,
                         String message,
                         Long tacheId,
                         Long projetId) {

        if (destinataire == null) return;
        if (Boolean.FALSE.equals(destinataire.getActif())) return;

        Long acteur = serviceSecurite.idCourant();
        if (acteur != null && acteur.equals(destinataire.getId())) {
            return;
        }

        Notification notification = new Notification();
        notification.setDestinataire(destinataire);
        notification.setType(type);
        notification.setMessage(tronquer(message));
        notification.setTacheId(tacheId);
        notification.setProjetId(projetId);
        notification.setLu(false);

        notificationRepository.save(notification);
    }

    /** Nom de la personne connectee, pour composer les messages. */
    public String nomActeur() {
        return serviceSecurite.utilisateurCourant()
                .map(Utilisateur::getNomComplet)
                .orElse("Quelqu'un");
    }

    // ---------- Lecture ----------

    @Transactional(readOnly = true)
    public List<NotificationDTO> mesNotifications() {
        Long id = exigerUtilisateurConnecte();
        return notificationRepository
                .findByDestinataireIdOrderByDateCreationDesc(
                        id, PageRequest.of(0, TAILLE_HISTORIQUE))
                .stream()
                .map(NotificationDTO::depuis)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long nombreNonLues() {
        return notificationRepository.countByDestinataireIdAndLuFalse(
                exigerUtilisateurConnecte());
    }

    // ---------- Mise a jour ----------

    public NotificationDTO marquerLue(Long notificationId) {
        Long moi = exigerUtilisateurConnecte();

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> RessourceIntrouvableException.pour(
                        "Notification", notificationId));

        if (!notification.getDestinataire().getId().equals(moi)) {
            throw new RegleMetierException(
                    "Cette notification ne vous est pas destinee");
        }

        notification.setLu(true);
        return NotificationDTO.depuis(notificationRepository.save(notification));
    }

    public int toutMarquerLu() {
        return notificationRepository.marquerToutLu(exigerUtilisateurConnecte());
    }

    public void supprimer(Long notificationId) {
        Long moi = exigerUtilisateurConnecte();

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> RessourceIntrouvableException.pour(
                        "Notification", notificationId));

        if (!notification.getDestinataire().getId().equals(moi)) {
            throw new RegleMetierException(
                    "Cette notification ne vous est pas destinee");
        }

        notificationRepository.delete(notification);
    }

    // ---------------------------------------------------------------

    private Long exigerUtilisateurConnecte() {
        Long id = serviceSecurite.idCourant();
        if (id == null) {
            throw new RegleMetierException("Aucun utilisateur connecte");
        }
        return id;
    }

    private String tronquer(String texte) {
        if (texte == null) return "";
        return texte.length() <= 300 ? texte : texte.substring(0, 297) + "…";
    }
}

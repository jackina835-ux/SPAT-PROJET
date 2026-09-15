package mg.spat.gestion_projets.controller;

import mg.spat.gestion_projets.dto.NotificationDTO;
import mg.spat.gestion_projets.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Notifications de l'utilisateur connecte.
 *
 * Aucune annotation @PreAuthorize ici : le service ne travaille
 * que sur l'identite portee par le jeton. Personne ne peut
 * demander les notifications de quelqu'un d'autre, puisque
 * l'identifiant n'est jamais passe en parametre.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /** GET /api/notifications */
    @GetMapping
    public List<NotificationDTO> mesNotifications() {
        return notificationService.mesNotifications();
    }

    /** GET /api/notifications/non-lues */
    @GetMapping("/non-lues")
    public Map<String, Long> nombreNonLues() {
        return Map.of("nombre", notificationService.nombreNonLues());
    }

    /** PATCH /api/notifications/7/lue */
    @PatchMapping("/{id}/lue")
    public NotificationDTO marquerLue(@PathVariable Long id) {
        return notificationService.marquerLue(id);
    }

    /** PATCH /api/notifications/tout-lu */
    @PatchMapping("/tout-lu")
    public Map<String, Integer> toutMarquerLu() {
        return Map.of("marquees", notificationService.toutMarquerLu());
    }

    /** DELETE /api/notifications/7 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        notificationService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}

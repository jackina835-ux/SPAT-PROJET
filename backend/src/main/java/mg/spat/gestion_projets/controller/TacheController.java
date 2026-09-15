package mg.spat.gestion_projets.controller;

import jakarta.validation.Valid;
import mg.spat.gestion_projets.dto.ChangementStatutDTO;
import mg.spat.gestion_projets.dto.KanbanDTO;
import mg.spat.gestion_projets.dto.TacheCreationDTO;
import mg.spat.gestion_projets.dto.TacheDTO;
import mg.spat.gestion_projets.service.TacheService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Points d'entree HTTP pour les taches.
 *
 * Droits appliques :
 *   consultation     : tout utilisateur connecte
 *   creation         : administrateur ou responsable du projet
 *   modification     : administrateur ou responsable du projet
 *   suppression      : administrateur ou responsable du projet
 *   assignation      : administrateur ou responsable du projet
 *   changement de statut : administrateur, responsable du projet,
 *                          ou personne a qui la tache est assignee
 *
 * Le changement de statut est volontairement plus ouvert :
 * c'est l'action qu'un membre d'equipe fait tous les jours en
 * deplacant ses cartes dans le Kanban.
 */
@RestController
@RequestMapping("/api/taches")
public class TacheController {

    private final TacheService tacheService;

    public TacheController(TacheService tacheService) {
        this.tacheService = tacheService;
    }

    /** GET /api/taches/5 */
    @GetMapping("/{id}")
    public TacheDTO consulter(@PathVariable Long id) {
        return tacheService.consulter(id);
    }

    /** GET /api/taches/projet/1 */
    @GetMapping("/projet/{projetId}")
    public List<TacheDTO> listerParProjet(@PathVariable Long projetId) {
        return tacheService.listerParProjet(projetId);
    }

    /** GET /api/taches/projet/1/kanban */
    @GetMapping("/projet/{projetId}/kanban")
    public KanbanDTO kanban(@PathVariable Long projetId) {
        return tacheService.construireKanban(projetId);
    }

    /** GET /api/taches/projet/1/en-retard */
    @GetMapping("/projet/{projetId}/en-retard")
    public List<TacheDTO> listerEnRetard(@PathVariable Long projetId) {
        return tacheService.listerEnRetard(projetId);
    }

    /** GET /api/taches/utilisateur/3 */
    @GetMapping("/utilisateur/{utilisateurId}")
    public List<TacheDTO> listerParUtilisateur(@PathVariable Long utilisateurId) {
        return tacheService.listerParUtilisateur(utilisateurId);
    }

    /** GET /api/taches/5/sous-taches */
    @GetMapping("/{id}/sous-taches")
    public List<TacheDTO> listerSousTaches(@PathVariable Long id) {
        return tacheService.listerSousTaches(id);
    }

    /** POST /api/taches */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') "
                + "or @securite.estResponsableDuProjet(#demande.projetId)")
    public ResponseEntity<TacheDTO> creer(@Valid @RequestBody TacheCreationDTO demande) {
        TacheDTO creee = tacheService.creer(demande);
        return ResponseEntity.status(HttpStatus.CREATED).body(creee);
    }

    /** PUT /api/taches/5 */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') "
                + "or @securite.estResponsableDuProjetDeLaTache(#id)")
    public TacheDTO modifier(@PathVariable Long id,
                             @Valid @RequestBody TacheCreationDTO demande) {
        return tacheService.modifier(id, demande);
    }

    /**
     * PATCH /api/taches/5/statut
     * Appele a chaque deplacement de carte dans le Kanban.
     */
    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasRole('ADMIN') or @securite.peutChangerStatut(#id)")
    public TacheDTO changerStatut(@PathVariable Long id,
                                  @Valid @RequestBody ChangementStatutDTO demande) {
        return tacheService.changerStatut(id, demande.getStatut());
    }

    /** PATCH /api/taches/5/assignation/3 */
    @PatchMapping("/{tacheId}/assignation/{utilisateurId}")
    @PreAuthorize("hasRole('ADMIN') "
                + "or @securite.estResponsableDuProjetDeLaTache(#tacheId)")
    public TacheDTO assigner(@PathVariable Long tacheId,
                             @PathVariable Long utilisateurId) {
        return tacheService.assigner(tacheId, utilisateurId);
    }

    /** DELETE /api/taches/5/assignation */
    @DeleteMapping("/{tacheId}/assignation")
    @PreAuthorize("hasRole('ADMIN') "
                + "or @securite.estResponsableDuProjetDeLaTache(#tacheId)")
    public TacheDTO desassigner(@PathVariable Long tacheId) {
        return tacheService.desassigner(tacheId);
    }

    /** DELETE /api/taches/5 */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') "
                + "or @securite.estResponsableDuProjetDeLaTache(#id)")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        tacheService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}

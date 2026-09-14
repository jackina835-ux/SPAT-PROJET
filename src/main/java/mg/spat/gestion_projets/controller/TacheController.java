package mg.spat.gestion_projets.controller;

import jakarta.validation.Valid;
import mg.spat.gestion_projets.dto.ChangementStatutDTO;
import mg.spat.gestion_projets.dto.KanbanDTO;
import mg.spat.gestion_projets.dto.TacheCreationDTO;
import mg.spat.gestion_projets.dto.TacheDTO;
import mg.spat.gestion_projets.service.TacheService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<TacheDTO> creer(@Valid @RequestBody TacheCreationDTO demande) {
        TacheDTO creee = tacheService.creer(demande);
        return ResponseEntity.status(HttpStatus.CREATED).body(creee);
    }

    /** PUT /api/taches/5 */
    @PutMapping("/{id}")
    public TacheDTO modifier(@PathVariable Long id,
                             @Valid @RequestBody TacheCreationDTO demande) {
        return tacheService.modifier(id, demande);
    }

    /**
     * PATCH /api/taches/5/statut
     * Appele a chaque deplacement de carte dans le Kanban.
     */
    @PatchMapping("/{id}/statut")
    public TacheDTO changerStatut(@PathVariable Long id,
                                  @Valid @RequestBody ChangementStatutDTO demande) {
        return tacheService.changerStatut(id, demande.getStatut());
    }

    /** PATCH /api/taches/5/assignation/3 */
    @PatchMapping("/{tacheId}/assignation/{utilisateurId}")
    public TacheDTO assigner(@PathVariable Long tacheId,
                             @PathVariable Long utilisateurId) {
        return tacheService.assigner(tacheId, utilisateurId);
    }

    /** DELETE /api/taches/5/assignation */
    @DeleteMapping("/{tacheId}/assignation")
    public TacheDTO desassigner(@PathVariable Long tacheId) {
        return tacheService.desassigner(tacheId);
    }

    /** DELETE /api/taches/5 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        tacheService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}

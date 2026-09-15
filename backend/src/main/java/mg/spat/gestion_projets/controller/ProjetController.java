package mg.spat.gestion_projets.controller;

import jakarta.validation.Valid;
import mg.spat.gestion_projets.dto.ProjetCreationDTO;
import mg.spat.gestion_projets.dto.ProjetDTO;
import mg.spat.gestion_projets.service.ProjetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Points d'entree HTTP pour les projets.
 *
 * Droits appliques :
 *   consultation  : tout utilisateur connecte
 *   creation      : administrateur ou chef de projet
 *   modification  : administrateur ou responsable du projet
 *   suppression   : administrateur ou responsable du projet
 *   membres       : administrateur ou responsable du projet
 */
@RestController
@RequestMapping("/api/projets")
public class ProjetController {

    private final ProjetService projetService;

    public ProjetController(ProjetService projetService) {
        this.projetService = projetService;
    }

    /** GET /api/projets */
    @GetMapping
    public List<ProjetDTO> lister() {
        return projetService.listerTous();
    }

    /** GET /api/projets/5 */
    @GetMapping("/{id}")
    public ProjetDTO consulter(@PathVariable Long id) {
        return projetService.consulter(id);
    }

    /** GET /api/projets/utilisateur/3 */
    @GetMapping("/utilisateur/{utilisateurId}")
    public List<ProjetDTO> listerParUtilisateur(@PathVariable Long utilisateurId) {
        return projetService.listerParUtilisateur(utilisateurId);
    }

    /** GET /api/projets/en-retard */
    @GetMapping("/en-retard")
    public List<ProjetDTO> listerEnRetard() {
        return projetService.listerEnRetard();
    }

    /** POST /api/projets */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET')")
    public ResponseEntity<ProjetDTO> creer(@Valid @RequestBody ProjetCreationDTO demande) {
        ProjetDTO cree = projetService.creer(demande);
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }

    /** PUT /api/projets/5 */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securite.estResponsableDuProjet(#id)")
    public ProjetDTO modifier(@PathVariable Long id,
                              @Valid @RequestBody ProjetCreationDTO demande) {
        return projetService.modifier(id, demande);
    }

    /** DELETE /api/projets/5 */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securite.estResponsableDuProjet(#id)")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        projetService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    /** POST /api/projets/5/membres/3 */
    @PostMapping("/{projetId}/membres/{utilisateurId}")
    @PreAuthorize("hasRole('ADMIN') or @securite.estResponsableDuProjet(#projetId)")
    public ProjetDTO ajouterMembre(@PathVariable Long projetId,
                                   @PathVariable Long utilisateurId) {
        return projetService.ajouterMembre(projetId, utilisateurId);
    }

    /** DELETE /api/projets/5/membres/3 */
    @DeleteMapping("/{projetId}/membres/{utilisateurId}")
    @PreAuthorize("hasRole('ADMIN') or @securite.estResponsableDuProjet(#projetId)")
    public ProjetDTO retirerMembre(@PathVariable Long projetId,
                                   @PathVariable Long utilisateurId) {
        return projetService.retirerMembre(projetId, utilisateurId);
    }
}

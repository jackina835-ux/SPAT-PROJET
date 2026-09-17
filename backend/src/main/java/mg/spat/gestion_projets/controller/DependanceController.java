package mg.spat.gestion_projets.controller;

import mg.spat.gestion_projets.dto.DependancesDTO;
import mg.spat.gestion_projets.dto.TacheLienDTO;
import mg.spat.gestion_projets.service.DependanceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Dependances entre taches.
 *
 * Droits :
 *   consulter : membre du projet, ou administrateur
 *   modifier  : chef du projet, ou administrateur
 *
 * Declarer qu'une tache en attend une autre engage tout le
 * planning : c'est une decision de chef de projet, pas une
 * action quotidienne.
 */
@RestController
@RequestMapping("/api/dependances")
public class DependanceController {

    private final DependanceService dependanceService;

    public DependanceController(DependanceService dependanceService) {
        this.dependanceService = dependanceService;
    }

    /** GET /api/dependances/tache/5 */
    @GetMapping("/tache/{tacheId}")
    @PreAuthorize("hasRole('ADMIN') or @securite.estMembreDuProjetDeLaTache(#tacheId)")
    public DependancesDTO consulter(@PathVariable Long tacheId) {
        return dependanceService.consulter(tacheId);
    }

    /** GET /api/dependances/tache/5/candidates */
    @GetMapping("/tache/{tacheId}/candidates")
    @PreAuthorize("hasRole('ADMIN') or @securite.estResponsableDuProjetDeLaTache(#tacheId)")
    public List<TacheLienDTO> candidates(@PathVariable Long tacheId) {
        return dependanceService.candidates(tacheId);
    }

    /** POST /api/dependances/tache/5/depend-de/3 */
    @PostMapping("/tache/{tacheId}/depend-de/{dependDeId}")
    @PreAuthorize("hasRole('ADMIN') or @securite.estResponsableDuProjetDeLaTache(#tacheId)")
    public DependancesDTO ajouter(@PathVariable Long tacheId,
                                  @PathVariable Long dependDeId) {
        return dependanceService.ajouter(tacheId, dependDeId);
    }

    /** DELETE /api/dependances/tache/5/depend-de/3 */
    @DeleteMapping("/tache/{tacheId}/depend-de/{dependDeId}")
    @PreAuthorize("hasRole('ADMIN') or @securite.estResponsableDuProjetDeLaTache(#tacheId)")
    public DependancesDTO retirer(@PathVariable Long tacheId,
                                  @PathVariable Long dependDeId) {
        return dependanceService.retirer(tacheId, dependDeId);
    }
}

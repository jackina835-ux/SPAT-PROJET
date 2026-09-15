package mg.spat.gestion_projets.controller;

import jakarta.validation.Valid;
import mg.spat.gestion_projets.dto.RecapTempsDTO;
import mg.spat.gestion_projets.dto.SuiviTempsCreationDTO;
import mg.spat.gestion_projets.dto.SuiviTempsDTO;
import mg.spat.gestion_projets.service.SuiviTempsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Suivi du temps reellement passe.
 *
 * Droits :
 *   saisir     : membre du projet (verifie dans le service)
 *   consulter  : membre du projet, ou administrateur
 *   supprimer  : auteur de la saisie, ou chef du projet
 */
@RestController
@RequestMapping("/api/temps")
public class SuiviTempsController {

    private final SuiviTempsService suiviTempsService;

    public SuiviTempsController(SuiviTempsService suiviTempsService) {
        this.suiviTempsService = suiviTempsService;
    }

    /** POST /api/temps */
    @PostMapping
    public ResponseEntity<SuiviTempsDTO> saisir(
            @Valid @RequestBody SuiviTempsCreationDTO demande) {
        SuiviTempsDTO saisie = suiviTempsService.saisir(demande);
        return ResponseEntity.status(HttpStatus.CREATED).body(saisie);
    }

    /** GET /api/temps/tache/5 */
    @GetMapping("/tache/{tacheId}")
    @PreAuthorize("hasRole('ADMIN') or @securite.estMembreDuProjetDeLaTache(#tacheId)")
    public RecapTempsDTO recapTache(@PathVariable Long tacheId) {
        return suiviTempsService.recapTache(tacheId);
    }

    /** GET /api/temps/projet/1 */
    @GetMapping("/projet/{projetId}")
    @PreAuthorize("hasRole('ADMIN') or @securite.estMembreDuProjet(#projetId)")
    public RecapTempsDTO recapProjet(@PathVariable Long projetId) {
        return suiviTempsService.recapProjet(projetId);
    }

    /** GET /api/temps/moi */
    @GetMapping("/moi")
    public List<SuiviTempsDTO> mesSaisies() {
        return suiviTempsService.mesSaisies();
    }

    /** DELETE /api/temps/9 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        suiviTempsService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}

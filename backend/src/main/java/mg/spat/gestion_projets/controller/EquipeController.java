package mg.spat.gestion_projets.controller;

import mg.spat.gestion_projets.dto.ChargeEquipeDTO;
import mg.spat.gestion_projets.service.EquipeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Repartition de la charge de travail.
 *
 * Droits :
 *   charge d'un projet : tout membre du projet, plus l'administrateur
 *   charge globale     : administrateur et chefs de projet uniquement
 */
@RestController
@RequestMapping("/api/equipes")
public class EquipeController {

    private final EquipeService equipeService;

    public EquipeController(EquipeService equipeService) {
        this.equipeService = equipeService;
    }

    /** GET /api/equipes/projet/1/charge */
    @GetMapping("/projet/{projetId}/charge")
    @PreAuthorize("hasRole('ADMIN') or @securite.estMembreDuProjet(#projetId)")
    public ChargeEquipeDTO chargeDuProjet(@PathVariable Long projetId) {
        return equipeService.chargeDuProjet(projetId);
    }

    /** GET /api/equipes/charge */
    @GetMapping("/charge")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET')")
    public ChargeEquipeDTO chargeGlobale() {
        return equipeService.chargeGlobale();
    }
}

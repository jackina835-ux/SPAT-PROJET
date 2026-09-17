package mg.spat.gestion_projets.controller;

import mg.spat.gestion_projets.dto.UtilisateurSimpleDTO;
import mg.spat.gestion_projets.service.UtilisateurService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Annuaire des comptes utilisateurs.
 *
 * Droits : reserve a l'administrateur et aux chefs de projet, qui
 * sont les seuls a devoir choisir un utilisateur dans une liste
 * (affectation a un projet). Un membre n'a pas besoin de voir
 * l'annuaire complet.
 */
@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    /** GET /api/utilisateurs */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET')")
    public List<UtilisateurSimpleDTO> listerActifs() {
        return utilisateurService.listerActifs();
    }

    /** GET /api/utilisateurs/tous — ecran d'administration */
    @GetMapping("/tous")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UtilisateurSimpleDTO> listerTous() {
        return utilisateurService.listerTous();
    }

    /** PATCH /api/utilisateurs/5/activation */
    @PatchMapping("/{id}/activation")
    @PreAuthorize("hasRole('ADMIN')")
    public UtilisateurSimpleDTO basculerActivation(@PathVariable Long id,
                                                   Authentication authentification) {
        return utilisateurService.basculerActivation(id, authentification.getName());
    }
}

package mg.spat.gestion_projets.controller;

import jakarta.validation.Valid;
import mg.spat.gestion_projets.dto.ConnexionDTO;
import mg.spat.gestion_projets.dto.InscriptionDTO;
import mg.spat.gestion_projets.dto.ReponseConnexionDTO;
import mg.spat.gestion_projets.dto.UtilisateurSimpleDTO;
import mg.spat.gestion_projets.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** POST /api/auth/connexion */
    @PostMapping("/connexion")
    public ReponseConnexionDTO connecter(@Valid @RequestBody ConnexionDTO demande) {
        return authService.connecter(demande);
    }

    /** POST /api/auth/inscription */
    @PostMapping("/inscription")
    public ResponseEntity<UtilisateurSimpleDTO> inscrire(
            @Valid @RequestBody InscriptionDTO demande) {
        UtilisateurSimpleDTO cree = authService.inscrire(demande);
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }

    /**
     * GET /api/auth/moi
     * Renvoie le profil de l'utilisateur porteur du jeton.
     */
    @GetMapping("/moi")
    public UtilisateurSimpleDTO profil(Authentication authentification) {
        return authService.profil(authentification.getName());
    }

    /**
     * POST /api/auth/mot-de-passe
     * Corps : { "ancien": "...", "nouveau": "..." }
     */
    @PostMapping("/mot-de-passe")
    public ResponseEntity<Void> changerMotDePasse(Authentication authentification,
                                                  @RequestBody Map<String, String> corps) {
        authService.changerMotDePasse(
                authentification.getName(),
                corps.get("ancien"),
                corps.get("nouveau"));
        return ResponseEntity.noContent().build();
    }
}

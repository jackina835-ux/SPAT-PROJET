package mg.spat.gestion_projets.controller;

import jakarta.validation.Valid;
import mg.spat.gestion_projets.dto.CommentaireCreationDTO;
import mg.spat.gestion_projets.dto.CommentaireDTO;
import mg.spat.gestion_projets.service.CommentaireService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/commentaires")
public class CommentaireController {

    private final CommentaireService commentaireService;

    public CommentaireController(CommentaireService commentaireService) {
        this.commentaireService = commentaireService;
    }

    /** GET /api/commentaires/tache/5 */
    @GetMapping("/tache/{tacheId}")
    public List<CommentaireDTO> listerParTache(@PathVariable Long tacheId) {
        return commentaireService.listerParTache(tacheId);
    }

    /** POST /api/commentaires */
    @PostMapping
    public ResponseEntity<CommentaireDTO> creer(
            @Valid @RequestBody CommentaireCreationDTO demande) {
        CommentaireDTO cree = commentaireService.creer(demande);
        return ResponseEntity.status(HttpStatus.CREATED).body(cree);
    }

    /**
     * PUT /api/commentaires/7
     * Corps attendu : { "auteurId": 2, "contenu": "texte corrige" }
     */
    @PutMapping("/{id}")
    public CommentaireDTO modifier(@PathVariable Long id,
                                   @RequestBody Map<String, Object> corps) {
        Long auteurId = Long.valueOf(String.valueOf(corps.get("auteurId")));
        String contenu = String.valueOf(corps.get("contenu"));
        return commentaireService.modifier(id, auteurId, contenu);
    }

    /** DELETE /api/commentaires/7 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        commentaireService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}

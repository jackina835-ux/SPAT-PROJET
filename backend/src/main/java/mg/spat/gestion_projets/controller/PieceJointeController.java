package mg.spat.gestion_projets.controller;

import mg.spat.gestion_projets.dto.PieceJointeDTO;
import mg.spat.gestion_projets.service.PieceJointeService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Pieces jointes des taches.
 */
@RestController
@RequestMapping("/api/pieces-jointes")
public class PieceJointeController {

    private final PieceJointeService pieceJointeService;

    public PieceJointeController(PieceJointeService pieceJointeService) {
        this.pieceJointeService = pieceJointeService;
    }

    /** POST /api/pieces-jointes/tache/5 */
    @PostMapping(value = "/tache/{tacheId}",
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PieceJointeDTO> deposer(
            @PathVariable Long tacheId,
            @RequestParam("fichier") MultipartFile fichier) {

        PieceJointeDTO deposee = pieceJointeService.deposer(tacheId, fichier);
        return ResponseEntity.status(HttpStatus.CREATED).body(deposee);
    }

    /** GET /api/pieces-jointes/tache/5 */
    @GetMapping("/tache/{tacheId}")
    @PreAuthorize("hasRole('ADMIN') or @securite.estMembreDuProjetDeLaTache(#tacheId)")
    public List<PieceJointeDTO> lister(@PathVariable Long tacheId) {
        return pieceJointeService.listerParTache(tacheId);
    }

    /** GET /api/pieces-jointes/9/telecharger */
    @GetMapping("/{id}/telecharger")
    public ResponseEntity<byte[]> telecharger(@PathVariable Long id) {
        PieceJointeService.FichierATelecharger fichier =
                pieceJointeService.preparerTelechargement(id);

        String nomEncode = URLEncoder
                .encode(fichier.nom(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(fichier.contenu().length)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + nomEncode)
                .body(fichier.contenu());
    }

    /** DELETE /api/pieces-jointes/9 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        pieceJointeService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
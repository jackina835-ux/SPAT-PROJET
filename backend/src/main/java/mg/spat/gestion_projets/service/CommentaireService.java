package mg.spat.gestion_projets.service;

import mg.spat.gestion_projets.dto.CommentaireCreationDTO;
import mg.spat.gestion_projets.dto.CommentaireDTO;
import mg.spat.gestion_projets.entity.Commentaire;
import mg.spat.gestion_projets.entity.Tache;
import mg.spat.gestion_projets.entity.Utilisateur;
import mg.spat.gestion_projets.exception.RegleMetierException;
import mg.spat.gestion_projets.exception.RessourceIntrouvableException;
import mg.spat.gestion_projets.repository.CommentaireRepository;
import mg.spat.gestion_projets.repository.TacheRepository;
import mg.spat.gestion_projets.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommentaireService {

    private final CommentaireRepository commentaireRepository;
    private final TacheRepository tacheRepository;
    private final UtilisateurRepository utilisateurRepository;

    public CommentaireService(CommentaireRepository commentaireRepository,
                              TacheRepository tacheRepository,
                              UtilisateurRepository utilisateurRepository) {
        this.commentaireRepository = commentaireRepository;
        this.tacheRepository = tacheRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Transactional(readOnly = true)
    public List<CommentaireDTO> listerParTache(Long tacheId) {
        if (!tacheRepository.existsById(tacheId)) {
            throw RessourceIntrouvableException.pour("Tache", tacheId);
        }
        return commentaireRepository.findByTacheIdOrderByDateCreationAsc(tacheId).stream()
                .map(CommentaireDTO::depuis)
                .collect(Collectors.toList());
    }

    public CommentaireDTO creer(CommentaireCreationDTO demande) {
        Tache tache = tacheRepository.findById(demande.getTacheId())
                .orElseThrow(() -> RessourceIntrouvableException.pour(
                        "Tache", demande.getTacheId()));

        Utilisateur auteur = utilisateurRepository.findById(demande.getAuteurId())
                .orElseThrow(() -> RessourceIntrouvableException.pour(
                        "Utilisateur", demande.getAuteurId()));

        if (Boolean.FALSE.equals(auteur.getActif())) {
            throw new RegleMetierException(
                    "Un compte desactive ne peut pas rediger de commentaire");
        }
        if (!tache.getProjet().getMembres().contains(auteur)) {
            throw new RegleMetierException(
                    "Seuls les membres du projet peuvent commenter ses taches");
        }

        Commentaire commentaire = new Commentaire();
        commentaire.setContenu(demande.getContenu());
        commentaire.setTache(tache);
        commentaire.setAuteur(auteur);

        return CommentaireDTO.depuis(commentaireRepository.save(commentaire));
    }

    /**
     * Seul l'auteur peut modifier son propre commentaire.
     */
    public CommentaireDTO modifier(Long id, Long auteurId, String nouveauContenu) {
        Commentaire commentaire = trouverOuEchouer(id);

        if (!commentaire.getAuteur().getId().equals(auteurId)) {
            throw new RegleMetierException(
                    "Seul l'auteur peut modifier son commentaire");
        }
        if (nouveauContenu == null || nouveauContenu.isBlank()) {
            throw new RegleMetierException(
                    "Le contenu du commentaire ne peut pas etre vide");
        }

        commentaire.setContenu(nouveauContenu);
        return CommentaireDTO.depuis(commentaireRepository.save(commentaire));
    }

    public void supprimer(Long id) {
        Commentaire commentaire = trouverOuEchouer(id);
        commentaireRepository.delete(commentaire);
    }

    private Commentaire trouverOuEchouer(Long id) {
        return commentaireRepository.findById(id)
                .orElseThrow(() -> RessourceIntrouvableException.pour("Commentaire", id));
    }
}

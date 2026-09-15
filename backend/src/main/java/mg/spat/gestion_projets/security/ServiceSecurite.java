package mg.spat.gestion_projets.security;

import mg.spat.gestion_projets.entity.Commentaire;
import mg.spat.gestion_projets.entity.Projet;
import mg.spat.gestion_projets.entity.Tache;
import mg.spat.gestion_projets.entity.Utilisateur;
import mg.spat.gestion_projets.repository.CommentaireRepository;
import mg.spat.gestion_projets.repository.ProjetRepository;
import mg.spat.gestion_projets.repository.TacheRepository;
import mg.spat.gestion_projets.repository.UtilisateurRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Regroupe les verifications de droits qui dependent des donnees.
 *
 * Les annotations @PreAuthorize des controleurs appellent ces
 * methodes par le nom du bean, par exemple :
 *
 *     @PreAuthorize("hasRole('ADMIN') or @securite.estResponsableDuProjet(#id)")
 *
 * Le prefixe "hasRole" suffit pour les droits lies au seul role.
 * Ce service sert aux cas ou il faut consulter la base : est-ce
 * bien SON projet, SA tache, SON commentaire.
 */
@Component("securite")
@Transactional(readOnly = true)
public class ServiceSecurite {

    private final UtilisateurRepository utilisateurRepository;
    private final ProjetRepository projetRepository;
    private final TacheRepository tacheRepository;
    private final CommentaireRepository commentaireRepository;

    public ServiceSecurite(UtilisateurRepository utilisateurRepository,
                           ProjetRepository projetRepository,
                           TacheRepository tacheRepository,
                           CommentaireRepository commentaireRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.projetRepository = projetRepository;
        this.tacheRepository = tacheRepository;
        this.commentaireRepository = commentaireRepository;
    }

    /** L'utilisateur porteur du jeton, ou vide si personne n'est connecte. */
    public Optional<Utilisateur> utilisateurCourant() {
        Authentication authentification =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentification == null || !authentification.isAuthenticated()) {
            return Optional.empty();
        }
        return utilisateurRepository.findByEmail(authentification.getName());
    }

    /** Identifiant de l'utilisateur connecte, ou null. */
    public Long idCourant() {
        return utilisateurCourant().map(Utilisateur::getId).orElse(null);
    }

    /** Vrai si l'utilisateur connecte est le responsable de ce projet. */
    public boolean estResponsableDuProjet(Long projetId) {
        Long id = idCourant();
        if (id == null || projetId == null) return false;

        return projetRepository.findById(projetId)
                .map(Projet::getResponsable)
                .map(Utilisateur::getId)
                .map(id::equals)
                .orElse(false);
    }

    /** Vrai si l'utilisateur connecte est membre de ce projet. */
    public boolean estMembreDuProjet(Long projetId) {
        Long id = idCourant();
        if (id == null || projetId == null) return false;

        return projetRepository.findById(projetId)
                .map(projet -> projet.getMembres().stream()
                        .anyMatch(m -> m.getId().equals(id)))
                .orElse(false);
    }

    /** Vrai si l'utilisateur connecte dirige le projet auquel cette tache appartient. */
    public boolean estResponsableDuProjetDeLaTache(Long tacheId) {
        if (tacheId == null) return false;

        return tacheRepository.findById(tacheId)
                .map(tache -> estResponsableDuProjet(tache.getProjet().getId()))
                .orElse(false);
    }

    /** Vrai si l'utilisateur connecte est membre du projet de cette tache. */
    public boolean estMembreDuProjetDeLaTache(Long tacheId) {
        if (tacheId == null) return false;

        return tacheRepository.findById(tacheId)
                .map(tache -> estMembreDuProjet(tache.getProjet().getId()))
                .orElse(false);
    }

    /** Vrai si la tache est assignee a l'utilisateur connecte. */
    public boolean estAssigneALaTache(Long tacheId) {
        Long id = idCourant();
        if (id == null || tacheId == null) return false;

        return tacheRepository.findById(tacheId)
                .map(Tache::getAssigneA)
                .map(Utilisateur::getId)
                .map(id::equals)
                .orElse(false);
    }

    /**
     * Droit de deplacer une carte dans le Kanban :
     * la personne a qui la tache est assignee, ou le chef du projet.
     */
    public boolean peutChangerStatut(Long tacheId) {
        return estAssigneALaTache(tacheId) || estResponsableDuProjetDeLaTache(tacheId);
    }

    /** Vrai si l'utilisateur connecte a redige ce commentaire. */
    public boolean estAuteurDuCommentaire(Long commentaireId) {
        Long id = idCourant();
        if (id == null || commentaireId == null) return false;

        return commentaireRepository.findById(commentaireId)
                .map(Commentaire::getAuteur)
                .map(Utilisateur::getId)
                .map(id::equals)
                .orElse(false);
    }

    /** Vrai si le commentaire porte sur une tache d'un projet que l'utilisateur dirige. */
    public boolean estResponsableDuProjetDuCommentaire(Long commentaireId) {
        if (commentaireId == null) return false;

        return commentaireRepository.findById(commentaireId)
                .map(c -> estResponsableDuProjet(c.getTache().getProjet().getId()))
                .orElse(false);
    }

    /** Vrai si l'identifiant passe est celui de l'utilisateur connecte. */
    public boolean estMoiMeme(Long utilisateurId) {
        Long id = idCourant();
        return id != null && id.equals(utilisateurId);
    }
}

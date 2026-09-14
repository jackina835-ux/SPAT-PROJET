package mg.spat.gestion_projets.service;

import mg.spat.gestion_projets.dto.ProjetCreationDTO;
import mg.spat.gestion_projets.dto.ProjetDTO;
import mg.spat.gestion_projets.entity.Projet;
import mg.spat.gestion_projets.entity.StatutProjet;
import mg.spat.gestion_projets.entity.Utilisateur;
import mg.spat.gestion_projets.exception.RegleMetierException;
import mg.spat.gestion_projets.exception.RessourceIntrouvableException;
import mg.spat.gestion_projets.repository.ProjetRepository;
import mg.spat.gestion_projets.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Couche metier des projets.
 * C'est ici que vivent les regles de gestion, pas dans le controleur.
 */
@Service
@Transactional
public class ProjetService {

    private final ProjetRepository projetRepository;
    private final UtilisateurRepository utilisateurRepository;

    public ProjetService(ProjetRepository projetRepository,
                         UtilisateurRepository utilisateurRepository) {
        this.projetRepository = projetRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjetDTO> listerTous() {
        return projetRepository.findAll().stream()
                .map(ProjetDTO::depuis)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjetDTO consulter(Long id) {
        return ProjetDTO.depuis(trouverOuEchouer(id));
    }

    @Transactional(readOnly = true)
    public List<ProjetDTO> listerParUtilisateur(Long utilisateurId) {
        return projetRepository.findProjetsDeLUtilisateur(utilisateurId).stream()
                .map(ProjetDTO::depuis)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjetDTO> listerEnRetard() {
        return projetRepository.findProjetsEnRetard().stream()
                .map(ProjetDTO::depuis)
                .collect(Collectors.toList());
    }

    public ProjetDTO creer(ProjetCreationDTO demande) {
        verifierCoherenceDesDates(demande);

        Utilisateur responsable = utilisateurRepository.findById(demande.getResponsableId())
                .orElseThrow(() -> RessourceIntrouvableException.pour(
                        "Utilisateur", demande.getResponsableId()));

        Projet projet = new Projet();
        projet.setNom(demande.getNom());
        projet.setDescription(demande.getDescription());
        projet.setDateDebut(demande.getDateDebut());
        projet.setDateFin(demande.getDateFin());
        projet.setStatut(demande.getStatut() == null
                ? StatutProjet.PLANIFIE
                : demande.getStatut());
        projet.setResponsable(responsable);

        // Le responsable devient automatiquement membre de son projet
        projet.ajouterMembre(responsable);

        return ProjetDTO.depuis(projetRepository.save(projet));
    }

    public ProjetDTO modifier(Long id, ProjetCreationDTO demande) {
        verifierCoherenceDesDates(demande);

        Projet projet = trouverOuEchouer(id);

        Utilisateur responsable = utilisateurRepository.findById(demande.getResponsableId())
                .orElseThrow(() -> RessourceIntrouvableException.pour(
                        "Utilisateur", demande.getResponsableId()));

        projet.setNom(demande.getNom());
        projet.setDescription(demande.getDescription());
        projet.setDateDebut(demande.getDateDebut());
        projet.setDateFin(demande.getDateFin());
        if (demande.getStatut() != null) {
            projet.setStatut(demande.getStatut());
        }
        projet.setResponsable(responsable);
        projet.ajouterMembre(responsable);

        return ProjetDTO.depuis(projetRepository.save(projet));
    }

    public void supprimer(Long id) {
        Projet projet = trouverOuEchouer(id);
        projetRepository.delete(projet);
    }

    public ProjetDTO ajouterMembre(Long projetId, Long utilisateurId) {
        Projet projet = trouverOuEchouer(projetId);
        Utilisateur membre = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> RessourceIntrouvableException.pour(
                        "Utilisateur", utilisateurId));

        if (Boolean.FALSE.equals(membre.getActif())) {
            throw new RegleMetierException(
                    "Impossible d'affecter un compte desactive a un projet");
        }
        if (projet.getMembres().contains(membre)) {
            throw new RegleMetierException(
                    "Cet utilisateur est deja membre du projet");
        }

        projet.ajouterMembre(membre);
        return ProjetDTO.depuis(projetRepository.save(projet));
    }

    public ProjetDTO retirerMembre(Long projetId, Long utilisateurId) {
        Projet projet = trouverOuEchouer(projetId);
        Utilisateur membre = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> RessourceIntrouvableException.pour(
                        "Utilisateur", utilisateurId));

        if (projet.getResponsable().getId().equals(utilisateurId)) {
            throw new RegleMetierException(
                    "Le responsable du projet ne peut pas etre retire des membres");
        }

        projet.retirerMembre(membre);
        return ProjetDTO.depuis(projetRepository.save(projet));
    }

    private Projet trouverOuEchouer(Long id) {
        return projetRepository.findById(id)
                .orElseThrow(() -> RessourceIntrouvableException.pour("Projet", id));
    }

    private void verifierCoherenceDesDates(ProjetCreationDTO demande) {
        if (demande.getDateDebut() != null
                && demande.getDateFin() != null
                && demande.getDateFin().isBefore(demande.getDateDebut())) {
            throw new RegleMetierException(
                    "La date de fin ne peut pas preceder la date de debut");
        }
    }
}

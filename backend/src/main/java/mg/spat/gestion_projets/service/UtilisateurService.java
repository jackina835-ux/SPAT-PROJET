package mg.spat.gestion_projets.service;

import mg.spat.gestion_projets.dto.UtilisateurSimpleDTO;
import mg.spat.gestion_projets.entity.Utilisateur;
import mg.spat.gestion_projets.exception.RegleMetierException;
import mg.spat.gestion_projets.exception.RessourceIntrouvableException;
import mg.spat.gestion_projets.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Consultation et activation des comptes utilisateurs.
 * La creation (inscription) reste dans AuthService.
 */
@Service
@Transactional(readOnly = true)
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * Comptes actifs, pour les ecrans qui doivent proposer un
     * utilisateur (affectation a un projet, assignation de tache).
     */
    public List<UtilisateurSimpleDTO> listerActifs() {
        return utilisateurRepository.findByActifTrue().stream()
                .map(UtilisateurSimpleDTO::depuis)
                .collect(Collectors.toList());
    }

    /** Tous les comptes, actifs ou non : pour l'ecran d'administration. */
    public List<UtilisateurSimpleDTO> listerTous() {
        return utilisateurRepository.findAll().stream()
                .sorted(Comparator.comparing(Utilisateur::getNom))
                .map(UtilisateurSimpleDTO::depuis)
                .collect(Collectors.toList());
    }

    /**
     * Active ou desactive un compte (bascule).
     * Un administrateur ne peut pas se desactiver lui-meme : il se
     * couperait l'acces a cet ecran.
     */
    @Transactional
    public UtilisateurSimpleDTO basculerActivation(Long id, String emailActeur) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> RessourceIntrouvableException.pour("Utilisateur", id));

        if (utilisateur.getEmail().equalsIgnoreCase(emailActeur)) {
            throw new RegleMetierException(
                    "Vous ne pouvez pas desactiver votre propre compte");
        }

        utilisateur.setActif(!Boolean.TRUE.equals(utilisateur.getActif()));
        return UtilisateurSimpleDTO.depuis(utilisateurRepository.save(utilisateur));
    }
}

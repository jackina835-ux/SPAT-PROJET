package mg.spat.gestion_projets.service;

import mg.spat.gestion_projets.dto.ConnexionDTO;
import mg.spat.gestion_projets.dto.InscriptionDTO;
import mg.spat.gestion_projets.dto.ReponseConnexionDTO;
import mg.spat.gestion_projets.dto.UtilisateurSimpleDTO;
import mg.spat.gestion_projets.entity.Utilisateur;
import mg.spat.gestion_projets.exception.RegleMetierException;
import mg.spat.gestion_projets.exception.RessourceIntrouvableException;
import mg.spat.gestion_projets.repository.UtilisateurRepository;
import mg.spat.gestion_projets.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Connexion et creation de comptes.
 */
@Service
@Transactional
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UtilisateurRepository utilisateurRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public ReponseConnexionDTO connecter(ConnexionDTO demande) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(demande.getEmail())
                .orElseThrow(() -> new RegleMetierException(
                        "Adresse electronique ou mot de passe incorrect"));

        // Le meme message dans les deux cas : ne jamais indiquer
        // si c'est l'email ou le mot de passe qui est faux.
        if (!passwordEncoder.matches(demande.getMotDePasse(), utilisateur.getMotDePasse())) {
            throw new RegleMetierException(
                    "Adresse electronique ou mot de passe incorrect");
        }

        if (Boolean.FALSE.equals(utilisateur.getActif())) {
            throw new RegleMetierException(
                    "Ce compte a ete desactive. Contactez l'administrateur.");
        }

        String jeton = jwtService.genererJeton(utilisateur);

        return new ReponseConnexionDTO(
                jeton,
                jwtService.getDureeValidite(),
                UtilisateurSimpleDTO.depuis(utilisateur));
    }

    public UtilisateurSimpleDTO inscrire(InscriptionDTO demande) {
        if (utilisateurRepository.existsByEmail(demande.getEmail())) {
            throw new RegleMetierException(
                    "Cette adresse electronique est deja utilisee");
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(demande.getNom());
        utilisateur.setPrenom(demande.getPrenom());
        utilisateur.setEmail(demande.getEmail());
        utilisateur.setMotDePasse(passwordEncoder.encode(demande.getMotDePasse()));
        utilisateur.setRole(demande.getRole());
        utilisateur.setActif(true);

        return UtilisateurSimpleDTO.depuis(utilisateurRepository.save(utilisateur));
    }

    @Transactional(readOnly = true)
    public UtilisateurSimpleDTO profil(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Aucun compte avec l'adresse " + email));
        return UtilisateurSimpleDTO.depuis(utilisateur);
    }

    public void changerMotDePasse(String email, String ancien, String nouveau) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Aucun compte avec l'adresse " + email));

        if (!passwordEncoder.matches(ancien, utilisateur.getMotDePasse())) {
            throw new RegleMetierException("L'ancien mot de passe est incorrect");
        }
        if (nouveau == null || nouveau.length() < 8) {
            throw new RegleMetierException(
                    "Le nouveau mot de passe doit contenir au moins 8 caracteres");
        }

        utilisateur.setMotDePasse(passwordEncoder.encode(nouveau));
        utilisateurRepository.save(utilisateur);
    }
}

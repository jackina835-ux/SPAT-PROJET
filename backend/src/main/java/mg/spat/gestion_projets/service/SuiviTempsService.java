package mg.spat.gestion_projets.service;

import mg.spat.gestion_projets.dto.RecapTempsDTO;
import mg.spat.gestion_projets.dto.SuiviTempsCreationDTO;
import mg.spat.gestion_projets.dto.SuiviTempsDTO;
import mg.spat.gestion_projets.entity.SuiviTemps;
import mg.spat.gestion_projets.entity.Tache;
import mg.spat.gestion_projets.entity.Utilisateur;
import mg.spat.gestion_projets.exception.RegleMetierException;
import mg.spat.gestion_projets.exception.RessourceIntrouvableException;
import mg.spat.gestion_projets.repository.ProjetRepository;
import mg.spat.gestion_projets.repository.SuiviTempsRepository;
import mg.spat.gestion_projets.repository.TacheRepository;
import mg.spat.gestion_projets.security.ServiceSecurite;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Saisie et consultation du temps reellement passe.
 *
 * Le temps circule en MINUTES dans tout le code. La conversion
 * en "2 h 30" se fait au dernier moment, a l'affichage.
 */
@Service
@Transactional
public class SuiviTempsService {

    /** Personne ne travaille plus de 16 h dans une journee. */
    private static final int MAX_MINUTES_PAR_JOUR = 16 * 60;

    private final SuiviTempsRepository suiviTempsRepository;
    private final TacheRepository tacheRepository;
    private final ProjetRepository projetRepository;
    private final ServiceSecurite serviceSecurite;

    public SuiviTempsService(SuiviTempsRepository suiviTempsRepository,
                             TacheRepository tacheRepository,
                             ProjetRepository projetRepository,
                             ServiceSecurite serviceSecurite) {
        this.suiviTempsRepository = suiviTempsRepository;
        this.tacheRepository = tacheRepository;
        this.projetRepository = projetRepository;
        this.serviceSecurite = serviceSecurite;
    }

    // ---------- Saisie ----------

    public SuiviTempsDTO saisir(SuiviTempsCreationDTO demande) {
        Utilisateur auteur = serviceSecurite.utilisateurCourant()
                .orElseThrow(() -> new RegleMetierException("Aucun utilisateur connecte"));

        Tache tache = tacheRepository.findById(demande.getTacheId())
                .orElseThrow(() -> RessourceIntrouvableException.pour(
                        "Tache", demande.getTacheId()));

        verifierDate(demande.getDateTravail());
        verifierMembreDuProjet(tache, auteur);
        verifierTotalDuJour(auteur, demande.getDateTravail(), demande.getDureeMinutes());

        SuiviTemps saisie = new SuiviTemps();
        saisie.setTache(tache);
        saisie.setUtilisateur(auteur);
        saisie.setDureeMinutes(demande.getDureeMinutes());
        saisie.setDateTravail(demande.getDateTravail());
        saisie.setCommentaire(demande.getCommentaire());

        return SuiviTempsDTO.depuis(suiviTempsRepository.save(saisie));
    }

    public void supprimer(Long saisieId) {
        Long moi = serviceSecurite.idCourant();

        SuiviTemps saisie = suiviTempsRepository.findById(saisieId)
                .orElseThrow(() -> RessourceIntrouvableException.pour(
                        "Saisie de temps", saisieId));

        boolean estAuteur = saisie.getUtilisateur().getId().equals(moi);
        boolean estChef = serviceSecurite.estResponsableDuProjet(
                saisie.getTache().getProjet().getId());

        if (!estAuteur && !estChef) {
            throw new RegleMetierException(
                    "Seul l'auteur de la saisie ou le chef de projet peut la supprimer");
        }

        suiviTempsRepository.delete(saisie);
    }

    // ---------- Consultation ----------

    @Transactional(readOnly = true)
    public RecapTempsDTO recapTache(Long tacheId) {
        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> RessourceIntrouvableException.pour("Tache", tacheId));

        int passe = suiviTempsRepository.totalMinutesParTache(tacheId);

        // chargeEstimee est exprimee en HEURES dans la table tache
        int estime = tache.getChargeEstimee() == null
                ? 0
                : tache.getChargeEstimee() * 60;

        List<SuiviTempsDTO> saisies = suiviTempsRepository
                .findByTacheIdOrderByDateTravailDesc(tacheId)
                .stream()
                .map(SuiviTempsDTO::depuis)
                .collect(Collectors.toList());

        return construireRecap(estime, passe, saisies);
    }

    @Transactional(readOnly = true)
    public RecapTempsDTO recapProjet(Long projetId) {
        if (!projetRepository.existsById(projetId)) {
            throw RessourceIntrouvableException.pour("Projet", projetId);
        }

        int passe = suiviTempsRepository.totalMinutesParProjet(projetId);

        int estime = tacheRepository.findByProjetId(projetId).stream()
                .map(Tache::getChargeEstimee)
                .filter(java.util.Objects::nonNull)
                .mapToInt(h -> h * 60)
                .sum();

        List<SuiviTempsDTO> saisies = suiviTempsRepository.findByProjetId(projetId)
                .stream()
                .map(SuiviTempsDTO::depuis)
                .collect(Collectors.toList());

        return construireRecap(estime, passe, saisies);
    }

    @Transactional(readOnly = true)
    public List<SuiviTempsDTO> mesSaisies() {
        Long moi = serviceSecurite.idCourant();
        if (moi == null) {
            throw new RegleMetierException("Aucun utilisateur connecte");
        }
        return suiviTempsRepository.findByUtilisateurIdOrderByDateTravailDesc(moi)
                .stream()
                .map(SuiviTempsDTO::depuis)
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------

    private RecapTempsDTO construireRecap(int estime, int passe,
                                          List<SuiviTempsDTO> saisies) {
        RecapTempsDTO recap = new RecapTempsDTO();
        recap.setEstimeMinutes(estime);
        recap.setPasseMinutes(passe);
        recap.setEstimeLisible(SuiviTempsDTO.enHeuresEtMinutes(estime));
        recap.setPasseLisible(SuiviTempsDTO.enHeuresEtMinutes(passe));
        recap.setSaisies(saisies);

        if (estime > 0) {
            recap.setConsommation(Math.round(1000.0 * passe / estime) / 10.0);
            recap.setDepassement(passe > estime);
        } else {
            // Sans estimation, aucun pourcentage n'a de sens.
            recap.setConsommation(null);
            recap.setDepassement(false);
        }
        return recap;
    }

    private void verifierDate(LocalDate date) {
        if (date.isAfter(LocalDate.now())) {
            throw new RegleMetierException(
                    "Impossible de saisir du temps sur une date future");
        }
        if (date.isBefore(LocalDate.now().minusMonths(3))) {
            throw new RegleMetierException(
                    "La saisie ne peut pas remonter a plus de trois mois");
        }
    }

    private void verifierMembreDuProjet(Tache tache, Utilisateur auteur) {
        boolean membre = tache.getProjet().getMembres().stream()
                .anyMatch(m -> m.getId().equals(auteur.getId()));

        if (!membre && !auteur.estAdministrateur()) {
            throw new RegleMetierException(
                    "Seuls les membres du projet peuvent saisir du temps sur ses taches");
        }
    }

    private void verifierTotalDuJour(Utilisateur auteur, LocalDate jour, int ajout) {
        int deja = suiviTempsRepository.totalMinutesDuJour(auteur.getId(), jour);

        if (deja + ajout > MAX_MINUTES_PAR_JOUR) {
            throw new RegleMetierException(
                    "Le total de la journee depasserait 16 heures ("
                  + SuiviTempsDTO.enHeuresEtMinutes(deja) + " deja saisies)");
        }
    }
}

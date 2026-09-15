package mg.spat.gestion_projets.service;

import mg.spat.gestion_projets.dto.ChargeEquipeDTO;
import mg.spat.gestion_projets.dto.ChargeMembreDTO;
import mg.spat.gestion_projets.dto.UtilisateurSimpleDTO;
import mg.spat.gestion_projets.entity.*;
import mg.spat.gestion_projets.exception.RessourceIntrouvableException;
import mg.spat.gestion_projets.repository.ProjetRepository;
import mg.spat.gestion_projets.repository.TacheRepository;
import mg.spat.gestion_projets.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Repartition de la charge de travail entre les membres.
 *
 * La charge d'une personne est calculee sur ses taches NON
 * TERMINEES : une tache achevee ne pese plus rien.
 */
@Service
@Transactional(readOnly = true)
public class EquipeService {

    /** Au dela de ce nombre d'heures restantes, on parle de surcharge. */
    private static final int SEUIL_SURCHARGE = 40;
    private static final int SEUIL_CHARGE = 20;
    private static final int SEUIL_NORMAL = 1;

    private final ProjetRepository projetRepository;
    private final TacheRepository tacheRepository;
    private final UtilisateurRepository utilisateurRepository;

    public EquipeService(ProjetRepository projetRepository,
                         TacheRepository tacheRepository,
                         UtilisateurRepository utilisateurRepository) {
        this.projetRepository = projetRepository;
        this.tacheRepository = tacheRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    /** Charge des membres d'un projet donne. */
    public ChargeEquipeDTO chargeDuProjet(Long projetId) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> RessourceIntrouvableException.pour("Projet", projetId));

        List<Tache> taches = tacheRepository.findByProjetId(projetId);

        List<ChargeMembreDTO> charges = new ArrayList<>();
        for (Utilisateur membre : projet.getMembres()) {
            List<Tache> siennes = taches.stream()
                    .filter(t -> t.getAssigneA() != null
                              && t.getAssigneA().getId().equals(membre.getId()))
                    .toList();
            charges.add(construireCharge(membre, siennes));
        }

        // Le plus charge en premier
        charges.sort(Comparator.comparingInt(ChargeMembreDTO::getHeuresEstimees).reversed());

        int nonAssignees = (int) taches.stream()
                .filter(t -> t.getAssigneA() == null
                          && t.getStatut() != StatutTache.TERMINEE)
                .count();

        int heuresTotal = charges.stream()
                .mapToInt(ChargeMembreDTO::getHeuresEstimees)
                .sum();

        ChargeEquipeDTO resultat = new ChargeEquipeDTO();
        resultat.setProjetId(projet.getId());
        resultat.setProjetNom(projet.getNom());
        resultat.setMembres(charges);
        resultat.setTachesNonAssignees(nonAssignees);
        resultat.setHeuresTotalActives(heuresTotal);
        resultat.setHeuresMoyennesParMembre(
                charges.isEmpty()
                        ? 0.0
                        : Math.round(10.0 * heuresTotal / charges.size()) / 10.0);
        return resultat;
    }

    /**
     * Charge globale : tous les comptes actifs, toutes taches
     * confondues, quel que soit le projet.
     */
    public ChargeEquipeDTO chargeGlobale() {
        List<Utilisateur> actifs = utilisateurRepository.findByActifTrue();

        List<ChargeMembreDTO> charges = new ArrayList<>();
        for (Utilisateur membre : actifs) {
            List<Tache> siennes = tacheRepository.findByAssigneAId(membre.getId());
            charges.add(construireCharge(membre, siennes));
        }

        charges.sort(Comparator.comparingInt(ChargeMembreDTO::getHeuresEstimees).reversed());

        int heuresTotal = charges.stream()
                .mapToInt(ChargeMembreDTO::getHeuresEstimees)
                .sum();

        ChargeEquipeDTO resultat = new ChargeEquipeDTO();
        resultat.setProjetNom("Ensemble des projets");
        resultat.setMembres(charges);
        resultat.setTachesNonAssignees(0);
        resultat.setHeuresTotalActives(heuresTotal);
        resultat.setHeuresMoyennesParMembre(
                charges.isEmpty()
                        ? 0.0
                        : Math.round(10.0 * heuresTotal / charges.size()) / 10.0);
        return resultat;
    }

    // ---------------------------------------------------------------

    private ChargeMembreDTO construireCharge(Utilisateur membre, List<Tache> taches) {
        Map<String, Long> parStatut = new LinkedHashMap<>();
        for (StatutTache statut : StatutTache.values()) {
            parStatut.put(statut.name(),
                    taches.stream().filter(t -> t.getStatut() == statut).count());
        }

        Map<String, Long> parPriorite = new LinkedHashMap<>();
        for (Priorite priorite : Priorite.values()) {
            parPriorite.put(priorite.name(),
                    taches.stream()
                          .filter(t -> t.getStatut() != StatutTache.TERMINEE)
                          .filter(t -> t.getPriorite() == priorite)
                          .count());
        }

        List<Tache> actives = taches.stream()
                .filter(t -> t.getStatut() != StatutTache.TERMINEE)
                .toList();

        int heures = actives.stream()
                .map(Tache::getChargeEstimee)
                .filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        int enRetard = (int) taches.stream().filter(Tache::estEnRetard).count();

        Integer joursAvantEcheance = actives.stream()
                .map(Tache::getDateEcheance)
                .filter(java.util.Objects::nonNull)
                .min(LocalDate::compareTo)
                .map(date -> (int) ChronoUnit.DAYS.between(LocalDate.now(), date))
                .orElse(null);

        ChargeMembreDTO dto = new ChargeMembreDTO();
        dto.setMembre(UtilisateurSimpleDTO.depuis(membre));
        dto.setTachesTotal(taches.size());
        dto.setTachesActives(actives.size());
        dto.setTachesTerminees(taches.size() - actives.size());
        dto.setTachesEnRetard(enRetard);
        dto.setParStatut(parStatut);
        dto.setParPriorite(parPriorite);
        dto.setHeuresEstimees(heures);
        dto.setProchaineEcheance(joursAvantEcheance);
        dto.setNiveau(determinerNiveau(heures, actives.size()));
        return dto;
    }

    /**
     * Traduit un volume d'heures en appreciation lisible.
     * Les seuils sont volontairement simples et ajustables.
     */
    private String determinerNiveau(int heures, int nombreTachesActives) {
        if (nombreTachesActives == 0) return "LIBRE";
        if (heures >= SEUIL_SURCHARGE) return "SURCHARGE";
        if (heures >= SEUIL_CHARGE) return "CHARGE";
        if (heures >= SEUIL_NORMAL) return "NORMAL";
        return "NORMAL";
    }
}

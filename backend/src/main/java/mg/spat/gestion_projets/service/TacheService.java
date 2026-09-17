package mg.spat.gestion_projets.service;

import mg.spat.gestion_projets.dto.KanbanDTO;
import mg.spat.gestion_projets.dto.TacheCreationDTO;
import mg.spat.gestion_projets.dto.TacheDTO;
import mg.spat.gestion_projets.entity.*;
import mg.spat.gestion_projets.exception.RegleMetierException;
import mg.spat.gestion_projets.exception.RessourceIntrouvableException;
import mg.spat.gestion_projets.repository.PieceJointeRepository;
import mg.spat.gestion_projets.repository.ProjetRepository;
import mg.spat.gestion_projets.repository.SuiviTempsRepository;
import mg.spat.gestion_projets.repository.TacheRepository;
import mg.spat.gestion_projets.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Couche metier des taches.
 */
@Service
@Transactional
public class TacheService {

    private final TacheRepository tacheRepository;
    private final ProjetRepository projetRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationService notificationService;
    private final SuiviTempsRepository suiviTempsRepository;
    private final PieceJointeRepository pieceJointeRepository;

    public TacheService(TacheRepository tacheRepository,
                        ProjetRepository projetRepository,
                        UtilisateurRepository utilisateurRepository,
                        NotificationService notificationService,
                        SuiviTempsRepository suiviTempsRepository,
                        PieceJointeRepository pieceJointeRepository) {
        this.tacheRepository = tacheRepository;
        this.projetRepository = projetRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.notificationService = notificationService;
        this.suiviTempsRepository = suiviTempsRepository;
        this.pieceJointeRepository = pieceJointeRepository;
    }

    @Transactional(readOnly = true)
    public TacheDTO consulter(Long id) {
        return TacheDTO.depuis(trouverOuEchouer(id));
    }

    @Transactional(readOnly = true)
    public List<TacheDTO> listerParProjet(Long projetId) {
        verifierProjetExiste(projetId);
        return tacheRepository.findByProjetId(projetId).stream()
                .map(TacheDTO::depuis)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TacheDTO> listerParUtilisateur(Long utilisateurId) {
        return tacheRepository.findByAssigneAId(utilisateurId).stream()
                .map(TacheDTO::depuis)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TacheDTO> listerSousTaches(Long tacheParentId) {
        return tacheRepository.findByTacheParentId(tacheParentId).stream()
                .map(TacheDTO::depuis)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TacheDTO> listerEnRetard(Long projetId) {
        verifierProjetExiste(projetId);
        return tacheRepository.findTachesEnRetard(projetId).stream()
                .map(TacheDTO::depuis)
                .collect(Collectors.toList());
    }

    /**
     * Vue complete du tableau Kanban : une entree par colonne,
     * meme quand la colonne est vide.
     */
    @Transactional(readOnly = true)
    public KanbanDTO construireKanban(Long projetId) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> RessourceIntrouvableException.pour("Projet", projetId));

        List<Tache> taches = tacheRepository.findByProjetId(projetId);

        // Un seul aller-retour en base pour chacun, plutot qu'un par
        // carte affichee sur le tableau.
        Map<Long, Integer> minutesParTache = new HashMap<>();
        for (Object[] ligne : suiviTempsRepository.sommeMinutesParTachePourProjet(projetId)) {
            minutesParTache.put((Long) ligne[0], ((Number) ligne[1]).intValue());
        }
        Map<Long, Integer> piecesJointesParTache = new HashMap<>();
        for (Object[] ligne : pieceJointeRepository.compterParTachePourProjet(projetId)) {
            piecesJointesParTache.put((Long) ligne[0], ((Number) ligne[1]).intValue());
        }

        Map<String, List<TacheDTO>> colonnes = new LinkedHashMap<>();
        Map<String, Long> compteurs = new LinkedHashMap<>();

        for (StatutTache statut : StatutTache.values()) {
            List<TacheDTO> contenu = taches.stream()
                    .filter(t -> t.getStatut() == statut)
                    .map(t -> {
                        TacheDTO dto = TacheDTO.depuis(t);
                        dto.setMinutesPassees(minutesParTache.getOrDefault(t.getId(), 0));
                        dto.setNombrePiecesJointes(piecesJointesParTache.getOrDefault(t.getId(), 0));
                        return dto;
                    })
                    .collect(Collectors.toList());
            colonnes.put(statut.name(), contenu);
            compteurs.put(statut.name(), (long) contenu.size());
        }

        long enRetard = taches.stream().filter(Tache::estEnRetard).count();

        KanbanDTO kanban = new KanbanDTO();
        kanban.setProjetId(projet.getId());
        kanban.setProjetNom(projet.getNom());
        kanban.setAvancement(projet.calculerAvancement());
        kanban.setColonnes(colonnes);
        kanban.setCompteurs(compteurs);
        kanban.setTotalTaches(taches.size());
        kanban.setTachesEnRetard((int) enRetard);
        return kanban;
    }

    public TacheDTO creer(TacheCreationDTO demande) {
        Projet projet = projetRepository.findById(demande.getProjetId())
                .orElseThrow(() -> RessourceIntrouvableException.pour(
                        "Projet", demande.getProjetId()));

        Tache tache = new Tache();
        tache.setProjet(projet);
        appliquer(demande, tache, projet);

        Tache enregistree = tacheRepository.save(tache);

        if (enregistree.getAssigneA() != null) {
            notificationService.notifier(
                    enregistree.getAssigneA(),
                    TypeNotification.ASSIGNATION,
                    notificationService.nomActeur()
                        + " vous a confie la tache \"" + enregistree.getTitre() + "\"",
                    enregistree.getId(),
                    projet.getId());
        }

        return TacheDTO.depuis(enregistree);
    }

    public TacheDTO modifier(Long id, TacheCreationDTO demande) {
        Tache tache = trouverOuEchouer(id);

        Projet projet = projetRepository.findById(demande.getProjetId())
                .orElseThrow(() -> RessourceIntrouvableException.pour(
                        "Projet", demande.getProjetId()));

        Utilisateur ancienAssigne = tache.getAssigneA();

        tache.setProjet(projet);
        appliquer(demande, tache, projet);

        Tache enregistree = tacheRepository.save(tache);
        Utilisateur nouvelAssigne = enregistree.getAssigneA();

        boolean assignationChangee =
                (ancienAssigne == null && nouvelAssigne != null)
             || (ancienAssigne != null && nouvelAssigne == null)
             || (ancienAssigne != null && nouvelAssigne != null
                 && !ancienAssigne.getId().equals(nouvelAssigne.getId()));

        if (assignationChangee) {
            if (nouvelAssigne != null) {
                notificationService.notifier(
                        nouvelAssigne,
                        TypeNotification.ASSIGNATION,
                        notificationService.nomActeur()
                            + " vous a confie la tache \"" + enregistree.getTitre() + "\"",
                        enregistree.getId(),
                        projet.getId());
            }
            if (ancienAssigne != null) {
                notificationService.notifier(
                        ancienAssigne,
                        TypeNotification.DESASSIGNATION,
                        notificationService.nomActeur()
                            + " vous a retire la tache \"" + enregistree.getTitre() + "\"",
                        enregistree.getId(),
                        projet.getId());
            }
        }

        return TacheDTO.depuis(enregistree);
    }

    /**
     * Appele a chaque deplacement de carte dans le Kanban.
     */
    public TacheDTO changerStatut(Long id, StatutTache nouveauStatut) {
        Tache tache = trouverOuEchouer(id);

        if (nouveauStatut == StatutTache.TERMINEE) {
            boolean sousTachesEnCours = tache.getSousTaches().stream()
                    .anyMatch(st -> st.getStatut() != StatutTache.TERMINEE);
            if (sousTachesEnCours) {
                throw new RegleMetierException(
                        "Impossible de terminer cette tache : certaines sous-taches "
                      + "ne sont pas encore terminees");
            }
        }

        // Une tache bloquee ne peut pas quitter la colonne "A faire".
        // Revenir en arriere reste toujours possible : on ne piege
        // jamais quelqu'un dans une colonne.
        if (nouveauStatut != StatutTache.A_FAIRE) {
            List<Tache> attendues = tache.dependancesNonTerminees();
            if (!attendues.isEmpty()) {
                String liste = attendues.stream()
                        .map(Tache::getTitre)
                        .collect(Collectors.joining(", "));
                throw new RegleMetierException(
                        "Cette tache attend : " + liste
                      + ". Terminez-la ou les d'abord.");
            }
        }

        tache.changerStatut(nouveauStatut);
        Tache enregistree = tacheRepository.save(tache);

        String texte = notificationService.nomActeur()
                + " a deplace \"" + enregistree.getTitre() + "\" vers "
                + libelle(nouveauStatut);

        // La personne assignee, et le chef du projet
        notificationService.notifier(
                enregistree.getAssigneA(),
                TypeNotification.CHANGEMENT_STATUT,
                texte,
                enregistree.getId(),
                enregistree.getProjet().getId());

        notificationService.notifier(
                enregistree.getProjet().getResponsable(),
                TypeNotification.CHANGEMENT_STATUT,
                texte,
                enregistree.getId(),
                enregistree.getProjet().getId());

        return TacheDTO.depuis(enregistree);
    }

    /** Libelle lisible d'un statut, pour les messages de notification. */
    private String libelle(StatutTache statut) {
        return switch (statut) {
            case A_FAIRE -> "A faire";
            case EN_COURS -> "En cours";
            case EN_REVISION -> "En revision";
            case TERMINEE -> "Terminee";
        };
    }

    public TacheDTO assigner(Long tacheId, Long utilisateurId) {
        Tache tache = trouverOuEchouer(tacheId);
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> RessourceIntrouvableException.pour(
                        "Utilisateur", utilisateurId));

        if (Boolean.FALSE.equals(utilisateur.getActif())) {
            throw new RegleMetierException(
                    "Impossible d'assigner une tache a un compte desactive");
        }
        if (!tache.getProjet().getMembres().contains(utilisateur)) {
            throw new RegleMetierException(
                    "Cet utilisateur n'est pas membre du projet de la tache");
        }

        tache.setAssigneA(utilisateur);
        Tache enregistree = tacheRepository.save(tache);

        notificationService.notifier(
                utilisateur,
                TypeNotification.ASSIGNATION,
                notificationService.nomActeur()
                    + " vous a confie la tache \"" + enregistree.getTitre() + "\"",
                enregistree.getId(),
                enregistree.getProjet().getId());

        return TacheDTO.depuis(enregistree);
    }

    public TacheDTO desassigner(Long tacheId) {
        Tache tache = trouverOuEchouer(tacheId);
        Utilisateur ancien = tache.getAssigneA();

        tache.setAssigneA(null);
        Tache enregistree = tacheRepository.save(tache);

        notificationService.notifier(
                ancien,
                TypeNotification.DESASSIGNATION,
                notificationService.nomActeur()
                    + " vous a retire la tache \"" + enregistree.getTitre() + "\"",
                enregistree.getId(),
                enregistree.getProjet().getId());

        return TacheDTO.depuis(enregistree);
    }

    public void supprimer(Long id) {
        Tache tache = trouverOuEchouer(id);
        tacheRepository.delete(tache);
    }

    // ---------------------------------------------------------------

    private void appliquer(TacheCreationDTO demande, Tache tache, Projet projet) {
        tache.setTitre(demande.getTitre());
        tache.setDescription(demande.getDescription());
        tache.setDateEcheance(demande.getDateEcheance());
        tache.setChargeEstimee(demande.getChargeEstimee());

        if (demande.getPriorite() != null) {
            tache.setPriorite(demande.getPriorite());
        }
        if (demande.getStatut() != null) {
            tache.setStatut(demande.getStatut());
        }

        verifierEcheanceDansLeProjet(demande, projet);
        rattacherResponsable(demande, tache, projet);
        rattacherTacheParente(demande, tache, projet);
    }

    private void verifierEcheanceDansLeProjet(TacheCreationDTO demande, Projet projet) {
        if (demande.getDateEcheance() != null
                && projet.getDateFin() != null
                && demande.getDateEcheance().isAfter(projet.getDateFin())) {
            throw new RegleMetierException(
                    "L'echeance de la tache depasse la date de fin du projet");
        }
    }

    private void rattacherResponsable(TacheCreationDTO demande, Tache tache, Projet projet) {
        if (demande.getAssigneAId() == null) {
            tache.setAssigneA(null);
            return;
        }
        Utilisateur responsable = utilisateurRepository.findById(demande.getAssigneAId())
                .orElseThrow(() -> RessourceIntrouvableException.pour(
                        "Utilisateur", demande.getAssigneAId()));

        if (!projet.getMembres().contains(responsable)) {
            throw new RegleMetierException(
                    "Cet utilisateur n'est pas membre du projet");
        }
        tache.setAssigneA(responsable);
    }

    private void rattacherTacheParente(TacheCreationDTO demande, Tache tache, Projet projet) {
        if (demande.getTacheParentId() == null) {
            tache.setTacheParent(null);
            return;
        }
        if (tache.getId() != null && tache.getId().equals(demande.getTacheParentId())) {
            throw new RegleMetierException(
                    "Une tache ne peut pas etre sa propre sous-tache");
        }

        Tache parente = tacheRepository.findById(demande.getTacheParentId())
                .orElseThrow(() -> RessourceIntrouvableException.pour(
                        "Tache", demande.getTacheParentId()));

        if (!parente.getProjet().getId().equals(projet.getId())) {
            throw new RegleMetierException(
                    "La tache parente doit appartenir au meme projet");
        }
        tache.setTacheParent(parente);
    }

    private Tache trouverOuEchouer(Long id) {
        return tacheRepository.findById(id)
                .orElseThrow(() -> RessourceIntrouvableException.pour("Tache", id));
    }

    private void verifierProjetExiste(Long projetId) {
        if (!projetRepository.existsById(projetId)) {
            throw RessourceIntrouvableException.pour("Projet", projetId);
        }
    }
}

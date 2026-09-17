package mg.spat.gestion_projets.service;

import mg.spat.gestion_projets.dto.DependancesDTO;
import mg.spat.gestion_projets.dto.TacheLienDTO;
import mg.spat.gestion_projets.entity.StatutTache;
import mg.spat.gestion_projets.entity.Tache;
import mg.spat.gestion_projets.exception.RegleMetierException;
import mg.spat.gestion_projets.exception.RessourceIntrouvableException;
import mg.spat.gestion_projets.repository.TacheRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Dependances entre taches : "B ne peut pas avancer tant que
 * A n'est pas terminee".
 *
 * Toute la difficulte tient en un mot : les cycles. Si A attend
 * B, que B attend C et que C attend A, aucune des trois ne
 * pourra jamais demarrer. Le systeme doit refuser ce cas avant
 * qu'il ne se produise.
 */
@Service
@Transactional
public class DependanceService {

    private final TacheRepository tacheRepository;

    public DependanceService(TacheRepository tacheRepository) {
        this.tacheRepository = tacheRepository;
    }

    @Transactional(readOnly = true)
    public DependancesDTO consulter(Long tacheId) {
        Tache tache = trouver(tacheId);
        return construire(tache);
    }

    /**
     * Declare que "tache" doit attendre "dependDe".
     */
    public DependancesDTO ajouter(Long tacheId, Long dependDeId) {
        if (tacheId.equals(dependDeId)) {
            throw new RegleMetierException(
                    "Une tache ne peut pas dependre d'elle-meme");
        }

        Tache tache = trouver(tacheId);
        Tache dependDe = trouver(dependDeId);

        if (!tache.getProjet().getId().equals(dependDe.getProjet().getId())) {
            throw new RegleMetierException(
                    "Les deux taches doivent appartenir au meme projet");
        }

        if (tache.getDependances().contains(dependDe)) {
            throw new RegleMetierException(
                    "Cette dependance existe deja");
        }

        if (creeraitUnCycle(tache, dependDe)) {
            throw new RegleMetierException(
                    "Impossible : cette dependance creerait un cycle. "
                  + "\"" + dependDe.getTitre() + "\" attend deja, directement ou "
                  + "indirectement, la tache \"" + tache.getTitre() + "\".");
        }

        tache.getDependances().add(dependDe);
        tacheRepository.save(tache);

        return construire(tache);
    }

    public DependancesDTO retirer(Long tacheId, Long dependDeId) {
        Tache tache = trouver(tacheId);
        Tache dependDe = trouver(dependDeId);

        if (!tache.getDependances().remove(dependDe)) {
            throw new RegleMetierException("Cette dependance n'existe pas");
        }

        tacheRepository.save(tache);
        return construire(tache);
    }

    /**
     * Taches du projet qui peuvent servir de dependance :
     * toutes sauf elle-meme, celles deja liees, et celles qui
     * creeraient un cycle.
     */
    @Transactional(readOnly = true)
    public List<TacheLienDTO> candidates(Long tacheId) {
        Tache tache = trouver(tacheId);

        return tacheRepository.findByProjetId(tache.getProjet().getId()).stream()
                .filter(autre -> !autre.getId().equals(tacheId))
                .filter(autre -> !tache.getDependances().contains(autre))
                .filter(autre -> !creeraitUnCycle(tache, autre))
                .sorted(Comparator.comparing(Tache::getTitre))
                .map(TacheLienDTO::depuis)
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------

    /**
     * Detection de cycle.
     *
     * Ajouter "tache attend candidate" cree un cycle si, en
     * partant de candidate et en remontant ses propres attentes,
     * on retombe sur tache.
     *
     * Le parcours est iteratif plutot que recursif : une chaine
     * de dependances tres longue ferait deborder la pile
     * d'appels avec une methode recursive.
     */
    private boolean creeraitUnCycle(Tache tache, Tache candidate) {
        Set<Long> visitees = new HashSet<>();
        Deque<Tache> aExplorer = new ArrayDeque<>();
        aExplorer.push(candidate);

        while (!aExplorer.isEmpty()) {
            Tache courante = aExplorer.pop();

            if (courante.getId().equals(tache.getId())) {
                return true;
            }
            if (!visitees.add(courante.getId())) {
                continue;
            }
            for (Tache suivante : courante.getDependances()) {
                aExplorer.push(suivante);
            }
        }
        return false;
    }

    private DependancesDTO construire(Tache tache) {
        DependancesDTO dto = new DependancesDTO();
        dto.setTacheId(tache.getId());
        dto.setTacheTitre(tache.getTitre());

        dto.setDependances(tache.getDependances().stream()
                .sorted(Comparator.comparing(Tache::getTitre))
                .map(TacheLienDTO::depuis)
                .collect(Collectors.toList()));

        dto.setBloque(tache.getBloque().stream()
                .sorted(Comparator.comparing(Tache::getTitre))
                .map(TacheLienDTO::depuis)
                .collect(Collectors.toList()));

        int ouvertes = (int) tache.getDependances().stream()
                .filter(d -> d.getStatut() != StatutTache.TERMINEE)
                .count();

        dto.setNonTerminees(ouvertes);
        dto.setEstBloquee(ouvertes > 0);
        return dto;
    }

    private Tache trouver(Long id) {
        return tacheRepository.findById(id)
                .orElseThrow(() -> RessourceIntrouvableException.pour("Tache", id));
    }
}

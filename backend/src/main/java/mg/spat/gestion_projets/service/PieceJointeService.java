package mg.spat.gestion_projets.service;

import mg.spat.gestion_projets.dto.PieceJointeDTO;
import mg.spat.gestion_projets.entity.PieceJointe;
import mg.spat.gestion_projets.entity.Tache;
import mg.spat.gestion_projets.entity.TypeNotification;
import mg.spat.gestion_projets.entity.Utilisateur;
import mg.spat.gestion_projets.exception.RegleMetierException;
import mg.spat.gestion_projets.exception.RessourceIntrouvableException;
import mg.spat.gestion_projets.repository.PieceJointeRepository;
import mg.spat.gestion_projets.repository.TacheRepository;
import mg.spat.gestion_projets.security.ServiceSecurite;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Depot, consultation et suppression des pieces jointes.
 */
@Service
@Transactional
public class PieceJointeService {

    /** Au dela, une tache devient illisible. */
    private static final int MAX_FICHIERS_PAR_TACHE = 10;

    private final PieceJointeRepository pieceJointeRepository;
    private final TacheRepository tacheRepository;
    private final StockageService stockageService;
    private final ServiceSecurite serviceSecurite;
    private final NotificationService notificationService;

    public PieceJointeService(PieceJointeRepository pieceJointeRepository,
                              TacheRepository tacheRepository,
                              StockageService stockageService,
                              ServiceSecurite serviceSecurite,
                              NotificationService notificationService) {
        this.pieceJointeRepository = pieceJointeRepository;
        this.tacheRepository = tacheRepository;
        this.stockageService = stockageService;
        this.serviceSecurite = serviceSecurite;
        this.notificationService = notificationService;
    }

    /**
     * Tout ce qu'il faut pour renvoyer un fichier au navigateur.
     * Un simple transport de donnees, sans lien avec la base.
     */
    public record FichierATelecharger(byte[] contenu, String nom, String typeMime) {
    }

    public PieceJointeDTO deposer(Long tacheId, MultipartFile fichier) {
        Utilisateur deposant = serviceSecurite.utilisateurCourant()
                .orElseThrow(() -> new RegleMetierException("Aucun utilisateur connecte"));

        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> RessourceIntrouvableException.pour("Tache", tacheId));

        verifierMembre(tache, deposant);

        if (pieceJointeRepository.countByTacheId(tacheId) >= MAX_FICHIERS_PAR_TACHE) {
            throw new RegleMetierException(
                    "Cette tache atteint la limite de "
                  + MAX_FICHIERS_PAR_TACHE + " pieces jointes");
        }

        // Le fichier est ecrit en premier : si l'ecriture echoue,
        // aucune ligne inutile n'est creee en base.
        String nomStocke = stockageService.enregistrer(fichier);

        PieceJointe piece = new PieceJointe();
        piece.setTache(tache);
        piece.setDeposant(deposant);
        piece.setNomOriginal(nettoyerNom(fichier.getOriginalFilename()));
        piece.setNomStocke(nomStocke);
        piece.setTypeMime(fichier.getContentType() == null
                ? "application/octet-stream"
                : fichier.getContentType());
        piece.setTailleOctets(fichier.getSize());

        PieceJointe enregistree = pieceJointeRepository.save(piece);

        notificationService.notifier(
                tache.getAssigneA(),
                TypeNotification.COMMENTAIRE,
                deposant.getNomComplet()
                    + " a joint un fichier a \"" + tache.getTitre() + "\"",
                tache.getId(),
                tache.getProjet().getId());

        return PieceJointeDTO.depuis(enregistree);
    }

    @Transactional(readOnly = true)
    public List<PieceJointeDTO> listerParTache(Long tacheId) {
        if (!tacheRepository.existsById(tacheId)) {
            throw RessourceIntrouvableException.pour("Tache", tacheId);
        }
        return pieceJointeRepository.findByTacheIdOrderByDateDepotDesc(tacheId)
                .stream()
                .map(PieceJointeDTO::depuis)
                .collect(Collectors.toList());
    }

    /**
     * Prepare un fichier pour le telechargement.
     *
     * TOUT se passe dans cette seule methode, donc dans une seule
     * transaction : recherche, controle des droits, et lecture du
     * disque. Si le controle des droits etait fait ailleurs, les
     * relations chargees en differe (le projet, ses membres) ne
     * seraient plus accessibles et Java leverait une exception.
     */
    @Transactional(readOnly = true)
    public FichierATelecharger preparerTelechargement(Long id) {
        PieceJointe piece = pieceJointeRepository.findById(id)
                .orElseThrow(() -> RessourceIntrouvableException.pour("Piece jointe", id));

        Utilisateur demandeur = serviceSecurite.utilisateurCourant()
                .orElseThrow(() -> new RegleMetierException("Aucun utilisateur connecte"));

        verifierMembre(piece.getTache(), demandeur);

        Resource ressource = stockageService.lire(piece.getNomStocke());

        byte[] octets;
        try (InputStream flux = ressource.getInputStream()) {
            octets = flux.readAllBytes();
        } catch (IOException e) {
            throw new RegleMetierException(
                    "Impossible de lire le fichier sur le serveur");
        }

        return new FichierATelecharger(
                octets,
                piece.getNomOriginal(),
                piece.getTypeMime());
    }

    /** Resultat d'un nettoyage : ce qui a ete efface, et combien de place ca libere. */
    public record RapportNettoyage(int fichiersSupprimes, long octetsLiberes, List<String> noms) {
    }

    /**
     * Efface les fichiers presents sur le disque mais sans aucune
     * ligne en base (upload interrompu avant l'enregistrement de la
     * ligne, ligne supprimee manuellement en base, etc.).
     *
     * Reservee a l'administrateur : c'est une action de maintenance,
     * pas une action du quotidien.
     */
    @Transactional(readOnly = true)
    public RapportNettoyage nettoyerOrphelins() {
        Set<String> referencesEnBase = new HashSet<>(pieceJointeRepository.listerNomsStockes());
        Set<String> surLeDisque = stockageService.listerFichiers();

        List<String> orphelins = surLeDisque.stream()
                .filter(nom -> !referencesEnBase.contains(nom))
                .collect(Collectors.toList());

        long octetsLiberes = 0;
        for (String nom : orphelins) {
            octetsLiberes += stockageService.tailleFichier(nom);
            stockageService.effacer(nom);
        }

        return new RapportNettoyage(orphelins.size(), octetsLiberes, orphelins);
    }

    public void supprimer(Long id) {
        Long moi = serviceSecurite.idCourant();

        PieceJointe piece = pieceJointeRepository.findById(id)
                .orElseThrow(() -> RessourceIntrouvableException.pour("Piece jointe", id));

        boolean estDeposant = piece.getDeposant().getId().equals(moi);
        boolean estChef = serviceSecurite.estResponsableDuProjet(
                piece.getTache().getProjet().getId());

        if (!estDeposant && !estChef) {
            throw new RegleMetierException(
                    "Seul le deposant ou le chef de projet peut supprimer ce fichier");
        }

        String nomStocke = piece.getNomStocke();

        // La base d'abord, le disque ensuite : une ligne sans
        // fichier serait pire qu'un fichier sans ligne.
        pieceJointeRepository.delete(piece);
        stockageService.effacer(nomStocke);
    }

    // ---------------------------------------------------------------

    private void verifierMembre(Tache tache, Utilisateur utilisateur) {
        boolean membre = tache.getProjet().getMembres().stream()
                .anyMatch(m -> m.getId().equals(utilisateur.getId()));

        boolean responsable = tache.getProjet().getResponsable() != null
                && tache.getProjet().getResponsable().getId().equals(utilisateur.getId());

        if (!membre && !responsable && !utilisateur.estAdministrateur()) {
            throw new RegleMetierException(
                    "Seuls les membres du projet peuvent acceder a ce fichier");
        }
    }

    private String nettoyerNom(String nom) {
        if (nom == null || nom.isBlank()) return "fichier";
        String propre = nom.replaceAll("[\\\\/:*?\"<>|\\r\\n]", "_").trim();
        return propre.length() > 255 ? propre.substring(0, 255) : propre;
    }
}
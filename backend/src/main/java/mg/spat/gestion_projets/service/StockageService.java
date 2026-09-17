package mg.spat.gestion_projets.service;

import jakarta.annotation.PostConstruct;
import mg.spat.gestion_projets.exception.RegleMetierException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Ecriture et lecture des fichiers sur le disque.
 *
 * Cette classe ne connait rien aux taches ni aux utilisateurs :
 * elle ne sait que poser un fichier quelque part et le relire.
 * Toute la logique metier est dans PieceJointeService.
 */
@Service
public class StockageService {

    private static final Logger journal = LoggerFactory.getLogger(StockageService.class);

    /** Extensions acceptees. Tout le reste est refuse. */
    private static final Set<String> EXTENSIONS_AUTORISEES = Set.of(
            "pdf", "doc", "docx", "odt", "rtf", "txt",
            "xls", "xlsx", "ods", "csv",
            "ppt", "pptx", "odp",
            "png", "jpg", "jpeg", "gif", "webp", "svg",
            "zip", "rar", "7z"
    );

    /**
     * Extensions explicitement interdites, meme si quelqu'un
     * modifiait la liste ci-dessus. Un fichier executable
     * depose sur un serveur est une porte d'entree classique.
     */
    private static final Set<String> EXTENSIONS_INTERDITES = Set.of(
            "exe", "bat", "cmd", "com", "msi", "scr", "vbs",
            "js", "jar", "sh", "ps1", "dll", "jsp", "php"
    );

    private final Path dossier;

    public StockageService(@Value("${stockage.dossier}") String chemin) {
        this.dossier = Paths.get(chemin).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void preparer() {
        try {
            Files.createDirectories(dossier);
            journal.info("Dossier de stockage des pieces jointes : {}", dossier);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Impossible de creer le dossier de stockage : " + dossier, e);
        }
    }

    /**
     * Ecrit le fichier sur le disque sous un nom unique.
     * Renvoie ce nom, a conserver en base.
     */
    public String enregistrer(MultipartFile fichier) {
        if (fichier == null || fichier.isEmpty()) {
            throw new RegleMetierException("Le fichier est vide");
        }

        String nomOrigine = fichier.getOriginalFilename();
        if (nomOrigine == null || nomOrigine.isBlank()) {
            throw new RegleMetierException("Le fichier n'a pas de nom");
        }

        String extension = extensionDe(nomOrigine);
        verifierExtension(extension);

        String nomStocke = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);
        Path destination = dossier.resolve(nomStocke).normalize();

        // Garde-fou : la destination doit rester dans le dossier
        if (!destination.getParent().equals(dossier)) {
            throw new RegleMetierException("Chemin de fichier refuse");
        }

        try {
            Files.copy(fichier.getInputStream(), destination,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            journal.error("Echec de l'ecriture du fichier {}", nomOrigine, e);
            throw new RegleMetierException("Impossible d'enregistrer le fichier");
        }

        return nomStocke;
    }

    /** Relit un fichier pour le telechargement. */
    public Resource lire(String nomStocke) {
        try {
            Path chemin = dossier.resolve(nomStocke).normalize();

            if (!chemin.getParent().equals(dossier)) {
                throw new RegleMetierException("Chemin de fichier refuse");
            }

            Resource ressource = new UrlResource(chemin.toUri());
            if (!ressource.exists() || !ressource.isReadable()) {
                throw new RegleMetierException(
                        "Le fichier est introuvable sur le serveur");
            }
            return ressource;

        } catch (IOException e) {
            throw new RegleMetierException("Impossible de lire le fichier");
        }
    }

    /**
     * Efface un fichier du disque.
     * Un echec n'interrompt rien : la ligne en base est deja
     * supprimee, un fichier orphelin est sans consequence.
     */
    public void effacer(String nomStocke) {
        try {
            Files.deleteIfExists(dossier.resolve(nomStocke).normalize());
        } catch (IOException e) {
            journal.warn("Fichier non efface sur le disque : {}", nomStocke, e);
        }
    }

    /**
     * Noms de tous les fichiers presents dans le dossier de stockage.
     * Sert a reperer les fichiers orphelins : ceux qui trainent sur
     * le disque sans plus aucune ligne en base (upload interrompu,
     * ligne supprimee manuellement, etc.).
     */
    public Set<String> listerFichiers() {
        try (Stream<Path> flux = Files.list(dossier)) {
            return flux.filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toCollection(HashSet::new));
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Impossible de lister le dossier de stockage", e);
        }
    }

    /** Taille d'un fichier sur le disque, 0 si on ne peut pas la lire. */
    public long tailleFichier(String nomStocke) {
        try {
            return Files.size(dossier.resolve(nomStocke).normalize());
        } catch (IOException e) {
            return 0;
        }
    }

    // ---------------------------------------------------------------

    private String extensionDe(String nom) {
        int point = nom.lastIndexOf('.');
        if (point < 0 || point == nom.length() - 1) return "";
        return nom.substring(point + 1).toLowerCase(Locale.ROOT);
    }

    private void verifierExtension(String extension) {
        if (extension.isEmpty()) {
            throw new RegleMetierException(
                    "Les fichiers sans extension ne sont pas acceptes");
        }
        if (EXTENSIONS_INTERDITES.contains(extension)) {
            throw new RegleMetierException(
                    "Les fichiers ." + extension + " ne sont pas acceptes");
        }
        if (!EXTENSIONS_AUTORISEES.contains(extension)) {
            throw new RegleMetierException(
                    "Le format ." + extension + " n'est pas accepte");
        }
    }
}

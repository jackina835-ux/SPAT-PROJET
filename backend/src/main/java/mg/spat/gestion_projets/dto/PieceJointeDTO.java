package mg.spat.gestion_projets.dto;

import mg.spat.gestion_projets.entity.PieceJointe;

import java.time.LocalDateTime;

public class PieceJointeDTO {

    private Long id;
    private Long tacheId;
    private String nomOriginal;
    private String typeMime;
    private long tailleOctets;
    private String tailleLisible;
    private String categorie;
    private UtilisateurSimpleDTO deposant;
    private LocalDateTime dateDepot;

    public PieceJointeDTO() {
    }

    public static PieceJointeDTO depuis(PieceJointe p) {
        PieceJointeDTO dto = new PieceJointeDTO();
        dto.id = p.getId();
        dto.tacheId = p.getTache().getId();
        dto.nomOriginal = p.getNomOriginal();
        dto.typeMime = p.getTypeMime();
        dto.tailleOctets = p.getTailleOctets();
        dto.tailleLisible = enTailleLisible(p.getTailleOctets());
        dto.categorie = categoriser(p.getTypeMime());
        dto.deposant = UtilisateurSimpleDTO.depuis(p.getDeposant());
        dto.dateDepot = p.getDateDepot();
        return dto;
    }

    /** 1536000 octets devient "1,5 Mo". */
    public static String enTailleLisible(long octets) {
        if (octets < 1024) return octets + " o";
        if (octets < 1024 * 1024) {
            return String.format("%.0f Ko", octets / 1024.0);
        }
        return String.format("%.1f Mo", octets / (1024.0 * 1024.0))
                     .replace('.', ',');
    }

    /** Regroupe les types MIME en familles, pour l'icone. */
    private static String categoriser(String mime) {
        if (mime == null) return "AUTRE";
        if (mime.startsWith("image/")) return "IMAGE";
        if (mime.equals("application/pdf")) return "PDF";
        if (mime.contains("word") || mime.contains("opendocument.text")) return "DOCUMENT";
        if (mime.contains("sheet") || mime.contains("excel") || mime.equals("text/csv")) return "TABLEUR";
        if (mime.contains("presentation") || mime.contains("powerpoint")) return "PRESENTATION";
        if (mime.startsWith("text/")) return "TEXTE";
        if (mime.contains("zip") || mime.contains("compressed")) return "ARCHIVE";
        return "AUTRE";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTacheId() { return tacheId; }
    public void setTacheId(Long tacheId) { this.tacheId = tacheId; }

    public String getNomOriginal() { return nomOriginal; }
    public void setNomOriginal(String nomOriginal) { this.nomOriginal = nomOriginal; }

    public String getTypeMime() { return typeMime; }
    public void setTypeMime(String typeMime) { this.typeMime = typeMime; }

    public long getTailleOctets() { return tailleOctets; }
    public void setTailleOctets(long tailleOctets) { this.tailleOctets = tailleOctets; }

    public String getTailleLisible() { return tailleLisible; }
    public void setTailleLisible(String tailleLisible) { this.tailleLisible = tailleLisible; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public UtilisateurSimpleDTO getDeposant() { return deposant; }
    public void setDeposant(UtilisateurSimpleDTO deposant) { this.deposant = deposant; }

    public LocalDateTime getDateDepot() { return dateDepot; }
    public void setDateDepot(LocalDateTime dateDepot) { this.dateDepot = dateDepot; }
}

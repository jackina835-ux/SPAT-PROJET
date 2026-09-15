package mg.spat.gestion_projets.dto;

import mg.spat.gestion_projets.entity.SuiviTemps;

import java.time.LocalDate;

public class SuiviTempsDTO {

    private Long id;
    private Long tacheId;
    private String tacheTitre;
    private UtilisateurSimpleDTO utilisateur;
    private int dureeMinutes;
    private String dureeLisible;
    private LocalDate dateTravail;
    private String commentaire;

    public SuiviTempsDTO() {
    }

    public static SuiviTempsDTO depuis(SuiviTemps s) {
        SuiviTempsDTO dto = new SuiviTempsDTO();
        dto.id = s.getId();
        dto.tacheId = s.getTache().getId();
        dto.tacheTitre = s.getTache().getTitre();
        dto.utilisateur = UtilisateurSimpleDTO.depuis(s.getUtilisateur());
        dto.dureeMinutes = s.getDureeMinutes();
        dto.dureeLisible = enHeuresEtMinutes(s.getDureeMinutes());
        dto.dateTravail = s.getDateTravail();
        dto.commentaire = s.getCommentaire();
        return dto;
    }

    /** 105 minutes devient "1 h 45". */
    public static String enHeuresEtMinutes(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        if (h == 0) return m + " min";
        if (m == 0) return h + " h";
        return h + " h " + String.format("%02d", m);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTacheId() { return tacheId; }
    public void setTacheId(Long tacheId) { this.tacheId = tacheId; }

    public String getTacheTitre() { return tacheTitre; }
    public void setTacheTitre(String tacheTitre) { this.tacheTitre = tacheTitre; }

    public UtilisateurSimpleDTO getUtilisateur() { return utilisateur; }
    public void setUtilisateur(UtilisateurSimpleDTO utilisateur) { this.utilisateur = utilisateur; }

    public int getDureeMinutes() { return dureeMinutes; }
    public void setDureeMinutes(int dureeMinutes) { this.dureeMinutes = dureeMinutes; }

    public String getDureeLisible() { return dureeLisible; }
    public void setDureeLisible(String dureeLisible) { this.dureeLisible = dureeLisible; }

    public LocalDate getDateTravail() { return dateTravail; }
    public void setDateTravail(LocalDate dateTravail) { this.dateTravail = dateTravail; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}

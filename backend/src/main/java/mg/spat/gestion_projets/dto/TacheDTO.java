package mg.spat.gestion_projets.dto;

import mg.spat.gestion_projets.entity.Priorite;
import mg.spat.gestion_projets.entity.StatutTache;
import mg.spat.gestion_projets.entity.Tache;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Ce qui sort vers le frontend quand on demande une tache.
 */
public class TacheDTO {

    private Long id;
    private String titre;
    private String description;
    private Priorite priorite;
    private StatutTache statut;
    private LocalDate dateEcheance;
    private Integer chargeEstimee;
    private LocalDateTime dateCreation;

    private Long projetId;
    private String projetNom;

    private UtilisateurSimpleDTO assigneA;

    private Long tacheParentId;
    private int nombreSousTaches;
    private int nombreCommentaires;

    private boolean enRetard;

    public TacheDTO() {
    }

    public static TacheDTO depuis(Tache t) {
        TacheDTO dto = new TacheDTO();
        dto.id = t.getId();
        dto.titre = t.getTitre();
        dto.description = t.getDescription();
        dto.priorite = t.getPriorite();
        dto.statut = t.getStatut();
        dto.dateEcheance = t.getDateEcheance();
        dto.chargeEstimee = t.getChargeEstimee();
        dto.dateCreation = t.getDateCreation();
        dto.projetId = t.getProjet().getId();
        dto.projetNom = t.getProjet().getNom();
        dto.assigneA = UtilisateurSimpleDTO.depuis(t.getAssigneA());
        dto.tacheParentId = t.getTacheParent() == null ? null : t.getTacheParent().getId();
        dto.nombreSousTaches = t.getSousTaches() == null ? 0 : t.getSousTaches().size();
        dto.nombreCommentaires = t.getCommentaires() == null ? 0 : t.getCommentaires().size();
        dto.enRetard = t.estEnRetard();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Priorite getPriorite() { return priorite; }
    public void setPriorite(Priorite priorite) { this.priorite = priorite; }

    public StatutTache getStatut() { return statut; }
    public void setStatut(StatutTache statut) { this.statut = statut; }

    public LocalDate getDateEcheance() { return dateEcheance; }
    public void setDateEcheance(LocalDate dateEcheance) { this.dateEcheance = dateEcheance; }

    public Integer getChargeEstimee() { return chargeEstimee; }
    public void setChargeEstimee(Integer chargeEstimee) { this.chargeEstimee = chargeEstimee; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public Long getProjetId() { return projetId; }
    public void setProjetId(Long projetId) { this.projetId = projetId; }

    public String getProjetNom() { return projetNom; }
    public void setProjetNom(String projetNom) { this.projetNom = projetNom; }

    public UtilisateurSimpleDTO getAssigneA() { return assigneA; }
    public void setAssigneA(UtilisateurSimpleDTO assigneA) { this.assigneA = assigneA; }

    public Long getTacheParentId() { return tacheParentId; }
    public void setTacheParentId(Long tacheParentId) { this.tacheParentId = tacheParentId; }

    public int getNombreSousTaches() { return nombreSousTaches; }
    public void setNombreSousTaches(int nombreSousTaches) { this.nombreSousTaches = nombreSousTaches; }

    public int getNombreCommentaires() { return nombreCommentaires; }
    public void setNombreCommentaires(int nombreCommentaires) { this.nombreCommentaires = nombreCommentaires; }

    public boolean isEnRetard() { return enRetard; }
    public void setEnRetard(boolean enRetard) { this.enRetard = enRetard; }
}

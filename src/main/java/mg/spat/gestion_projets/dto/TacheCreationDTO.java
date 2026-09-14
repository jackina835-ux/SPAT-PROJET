package mg.spat.gestion_projets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import mg.spat.gestion_projets.entity.Priorite;
import mg.spat.gestion_projets.entity.StatutTache;

import java.time.LocalDate;

/**
 * Ce qui entre depuis le frontend quand on cree ou modifie une tache.
 */
public class TacheCreationDTO {

    @NotBlank(message = "Le titre de la tache est obligatoire")
    @Size(max = 200, message = "Le titre ne peut pas depasser 200 caracteres")
    private String titre;

    private String description;

    private Priorite priorite;

    private StatutTache statut;

    private LocalDate dateEcheance;

    @Positive(message = "La charge estimee doit etre un nombre positif")
    private Integer chargeEstimee;

    @NotNull(message = "Le projet est obligatoire")
    private Long projetId;

    /** Facultatif : une tache peut ne pas avoir de responsable. */
    private Long assigneAId;

    /** Facultatif : renseigne uniquement pour une sous-tache. */
    private Long tacheParentId;

    public TacheCreationDTO() {
    }

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

    public Long getProjetId() { return projetId; }
    public void setProjetId(Long projetId) { this.projetId = projetId; }

    public Long getAssigneAId() { return assigneAId; }
    public void setAssigneAId(Long assigneAId) { this.assigneAId = assigneAId; }

    public Long getTacheParentId() { return tacheParentId; }
    public void setTacheParentId(Long tacheParentId) { this.tacheParentId = tacheParentId; }
}

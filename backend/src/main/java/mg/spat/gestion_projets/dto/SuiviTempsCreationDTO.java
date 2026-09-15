package mg.spat.gestion_projets.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class SuiviTempsCreationDTO {

    @NotNull(message = "La tache est obligatoire")
    private Long tacheId;

    @NotNull(message = "La duree est obligatoire")
    @Positive(message = "La duree doit etre superieure a zero")
    @Max(value = 1440, message = "Une saisie ne peut pas depasser 24 heures")
    private Integer dureeMinutes;

    @NotNull(message = "La date de travail est obligatoire")
    private LocalDate dateTravail;

    @Size(max = 200, message = "Le commentaire ne peut pas depasser 200 caracteres")
    private String commentaire;

    public SuiviTempsCreationDTO() {
    }

    public Long getTacheId() { return tacheId; }
    public void setTacheId(Long tacheId) { this.tacheId = tacheId; }

    public Integer getDureeMinutes() { return dureeMinutes; }
    public void setDureeMinutes(Integer dureeMinutes) { this.dureeMinutes = dureeMinutes; }

    public LocalDate getDateTravail() { return dateTravail; }
    public void setDateTravail(LocalDate dateTravail) { this.dateTravail = dateTravail; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}

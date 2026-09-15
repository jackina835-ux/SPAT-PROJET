package mg.spat.gestion_projets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import mg.spat.gestion_projets.entity.StatutProjet;

import java.time.LocalDate;

/**
 * Ce qui entre depuis le frontend quand on cree ou modifie un projet.
 * Les annotations de validation sont verifiees avant que le code
 * du controleur ne s'execute.
 */
public class ProjetCreationDTO {

    @NotBlank(message = "Le nom du projet est obligatoire")
    @Size(max = 150, message = "Le nom ne peut pas depasser 150 caracteres")
    private String nom;

    private String description;

    private LocalDate dateDebut;

    private LocalDate dateFin;

    private StatutProjet statut;

    @NotNull(message = "Le responsable est obligatoire")
    private Long responsableId;

    public ProjetCreationDTO() {
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public StatutProjet getStatut() { return statut; }
    public void setStatut(StatutProjet statut) { this.statut = statut; }

    public Long getResponsableId() { return responsableId; }
    public void setResponsableId(Long responsableId) { this.responsableId = responsableId; }
}

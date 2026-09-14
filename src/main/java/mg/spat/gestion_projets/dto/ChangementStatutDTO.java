package mg.spat.gestion_projets.dto;

import jakarta.validation.constraints.NotNull;
import mg.spat.gestion_projets.entity.StatutTache;

/**
 * Corps minimal envoye quand une carte est deplacee dans le Kanban.
 */
public class ChangementStatutDTO {

    @NotNull(message = "Le nouveau statut est obligatoire")
    private StatutTache statut;

    public ChangementStatutDTO() {
    }

    public StatutTache getStatut() { return statut; }
    public void setStatut(StatutTache statut) { this.statut = statut; }
}

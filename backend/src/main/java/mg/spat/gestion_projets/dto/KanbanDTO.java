package mg.spat.gestion_projets.dto;

import java.util.List;
import java.util.Map;

/**
 * Vue complete du tableau Kanban d'un projet :
 * les taches regroupees par colonne, plus les compteurs.
 */
public class KanbanDTO {

    private Long projetId;
    private String projetNom;
    private double avancement;
    private Map<String, List<TacheDTO>> colonnes;
    private Map<String, Long> compteurs;
    private int totalTaches;
    private int tachesEnRetard;

    public KanbanDTO() {
    }

    public Long getProjetId() { return projetId; }
    public void setProjetId(Long projetId) { this.projetId = projetId; }

    public String getProjetNom() { return projetNom; }
    public void setProjetNom(String projetNom) { this.projetNom = projetNom; }

    public double getAvancement() { return avancement; }
    public void setAvancement(double avancement) { this.avancement = avancement; }

    public Map<String, List<TacheDTO>> getColonnes() { return colonnes; }
    public void setColonnes(Map<String, List<TacheDTO>> colonnes) { this.colonnes = colonnes; }

    public Map<String, Long> getCompteurs() { return compteurs; }
    public void setCompteurs(Map<String, Long> compteurs) { this.compteurs = compteurs; }

    public int getTotalTaches() { return totalTaches; }
    public void setTotalTaches(int totalTaches) { this.totalTaches = totalTaches; }

    public int getTachesEnRetard() { return tachesEnRetard; }
    public void setTachesEnRetard(int tachesEnRetard) { this.tachesEnRetard = tachesEnRetard; }
}

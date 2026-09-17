package mg.spat.gestion_projets.dto;

import mg.spat.gestion_projets.entity.StatutTache;
import mg.spat.gestion_projets.entity.Tache;

/**
 * Vue minimale d'une tache, utilisee pour la citer a
 * l'interieur d'une autre reponse : dependances, blocages.
 *
 * Volontairement pauvre : si on renvoyait un TacheDTO complet,
 * chaque dependance renverrait ses propres dependances, et
 * ainsi de suite jusqu'a l'infini.
 */
public class TacheLienDTO {

    private Long id;
    private String titre;
    private StatutTache statut;
    private boolean terminee;
    private boolean enRetard;

    public TacheLienDTO() {
    }

    public static TacheLienDTO depuis(Tache t) {
        TacheLienDTO dto = new TacheLienDTO();
        dto.id = t.getId();
        dto.titre = t.getTitre();
        dto.statut = t.getStatut();
        dto.terminee = t.getStatut() == StatutTache.TERMINEE;
        dto.enRetard = t.estEnRetard();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public StatutTache getStatut() { return statut; }
    public void setStatut(StatutTache statut) { this.statut = statut; }

    public boolean isTerminee() { return terminee; }
    public void setTerminee(boolean terminee) { this.terminee = terminee; }

    public boolean isEnRetard() { return enRetard; }
    public void setEnRetard(boolean enRetard) { this.enRetard = enRetard; }
}
